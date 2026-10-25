package com.bancoexemplo.custody.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.application.port.PaymentRepository;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import com.bancoexemplo.custody.payments.domain.payment.exception.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.payment.exception.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.payment.exception.InvalidPaymentException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterPaymentServiceTest {

    private static final String AMOUNT_MESSAGE = "Valor deve ser igual ao da parcela: R$ 945,60";
    private static final String DATE_RANGE_MESSAGE = "Data do pagamento deve ficar entre a liberação e hoje";

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);
    private static final LocalDate RELEASE = LocalDate.of(2026, 9, 10);
    private static final LocalDate FIRST_DUE = LocalDate.of(2026, 11, 10);

    /** Released on 10/09/2026 with 12 installments of R$ 945.60 from 10/11/2026. */
    private static final UUID CONTRACT_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    /** Released on 10/09/2026, with the first installment due on 10/10/2026. */
    private static final UUID LATE_CONTRACT_ID = UUID.fromString("00000000-0000-4000-8000-000000000002");

    @Mock
    private ContractToPay contracts;

    @Mock
    private PaymentRepository payments;

    @Mock
    private PaymentEvents events;

    /** Service whose today, in Brasília, is the given date (15:00 UTC is midday there). */
    private RegisterPaymentService serviceOn(LocalDate today) {
        Clock clock = Clock.fixed(Instant.parse(today + "T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        return new RegisterPaymentService(contracts, payments, events, clock);
    }

    private void contractDueFrom(UUID contractId, LocalDate firstDue) {
        List<InstallmentTerms> installments = new ArrayList<>();
        for (int number = 1; number <= 12; number++) {
            installments.add(new InstallmentTerms(number, firstDue.plusMonths(number - 1L), new BigDecimal("945.60")));
        }
        when(contracts.find(contractId)).thenReturn(Optional.of(new ContractTerms(RELEASE, installments)));
    }

    private void onTimeContract() {
        contractDueFrom(CONTRACT_ID, FIRST_DUE);
    }

    private void lateContract() {
        contractDueFrom(LATE_CONTRACT_ID, LocalDate.of(2026, 10, 10));
    }

    private void alreadyPaid(Integer... installments) {
        when(payments.paidInstallments(CONTRACT_ID)).thenReturn(Set.of(installments));
    }

    private static RegisterPaymentCommand pay(int installment, String amount, LocalDate paidOn) {
        return new RegisterPaymentCommand(CONTRACT_ID, installment, paidOn, new BigDecimal(amount), "operador");
    }

    private static RegisterPaymentCommand payLate(String amount) {
        return new RegisterPaymentCommand(LATE_CONTRACT_ID, 1, TODAY, new BigDecimal(amount), "operador");
    }

    @Test
    @DisplayName("PAG-01 0007/CT-1: pagamento em dia da parcela mais antiga é aceito e guardado")
    void acceptsOnTimePayment() {
        onTimeContract();

        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        verify(payments).save(payment);
        assertThat(payment.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(payment.getInstallmentNumber()).isEqualTo(1);
        assertThat(payment.getPaidOn()).isEqualTo(TODAY);
        assertThat(payment.getAmount()).isEqualByComparingTo("945.60");
        assertThat(payment.getRegisteredBy()).isEqualTo("operador");
        assertThat(payment.getRegisteredAt()).isEqualTo(Instant.parse("2026-10-24T15:00:00Z"));
    }

    @Test
    @DisplayName("PAG-01 0007/CT-2: parcela 2 com a 1 em aberto é recusada")
    void refusesSkippingTheOldestUnpaid() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(2, "945.60", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Pague antes a parcela 1");
        verify(payments, never()).save(any());
    }

    @Test
    void nextInstallmentIsAcceptedAfterTheFirst() {
        onTimeContract();
        alreadyPaid(1);

        assertThat(serviceOn(TODAY).register(pay(2, "945.60", TODAY)).getInstallmentNumber())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("PAG-02 0007/CT-3: um centavo a menos é recusado")
    void refusesOneCentLess() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.59", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(AMOUNT_MESSAGE);
    }

    @Test
    @DisplayName("PAG-02 0007/CT-4: um centavo a mais é recusado")
    void refusesOneCentMore() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.61", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(AMOUNT_MESSAGE);
    }

    @Test
    @DisplayName("PAG-05 0008/CT-5: pagamento atrasado com multa e juros é aceito e guarda os dois")
    void acceptsLatePaymentWithCharges() {
        lateContract();

        Payment payment = serviceOn(TODAY).register(payLate("968.92"));

        assertThat(payment.getAmount()).isEqualByComparingTo("968.92");
        assertThat(payment.getLateFee()).isEqualByComparingTo("18.91");
        assertThat(payment.getDefaultInterest()).isEqualByComparingTo("4.41");
        verify(payments).save(payment);
    }

    @Test
    @DisplayName("PAG-02 0008/CT-6: pagamento atrasado sem multa e juros é recusado com o valor devido")
    void refusesLatePaymentWithoutCharges() {
        lateContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(payLate("945.60")))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Valor deve ser igual ao da parcela com multa e juros de mora: R$ 968,92");
    }

    @Test
    @DisplayName("PAG-06 0008/CT-7: a simulação traz parcela, dias, multa, juros e total")
    void quotesTheAmountDue() {
        lateContract();

        PaymentQuote quote = serviceOn(TODAY).quote(LATE_CONTRACT_ID, 1, TODAY);

        assertThat(quote.installmentNumber()).isEqualTo(1);
        assertThat(quote.installmentAmount()).isEqualByComparingTo("945.60");
        assertThat(quote.daysLate()).isEqualTo(14);
        assertThat(quote.lateFee()).isEqualByComparingTo("18.91");
        assertThat(quote.defaultInterest()).isEqualByComparingTo("4.41");
        assertThat(quote.total()).isEqualByComparingTo("968.92");
    }

    @Test
    void quoteAppliesTheSameRefusals() {
        onTimeContract();
        RegisterPaymentService service = serviceOn(TODAY);

        assertThatThrownBy(() -> service.quote(CONTRACT_ID, 2, TODAY))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Pague antes a parcela 1");
        alreadyPaid(1);
        assertThatThrownBy(() -> service.quote(CONTRACT_ID, 1, TODAY))
                .isInstanceOf(InstallmentAlreadyPaidException.class);
        assertThatThrownBy(() -> service.quote(CONTRACT_ID, 2, TODAY.plusDays(1)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
        assertThatThrownBy(() -> service.quote(UUID.randomUUID(), 1, TODAY))
                .isInstanceOf(ContractNotFoundForPaymentException.class);
    }

    @Test
    void onTimePaymentHasNoCharges() {
        onTimeContract();

        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThat(payment.getLateFee()).isEqualByComparingTo("0.00");
        assertThat(payment.getDefaultInterest()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("PAG-03 0007/CT-6: pagamento no dia do vencimento é aceito")
    void acceptsPaymentOnTheDueDate() {
        onTimeContract();

        assertThat(serviceOn(FIRST_DUE).register(pay(1, "945.60", FIRST_DUE)).getPaidOn())
                .isEqualTo(FIRST_DUE);
    }

    @Test
    @DisplayName("PAG-03 0007/CT-7: pagamento com data de amanhã é recusado")
    void refusesFutureDate() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", TODAY.plusDays(1))))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
    }

    @Test
    void atElevenAtNightInBrasiliaTomorrowIsStillTheFuture() {
        onTimeContract();
        Clock lateNight = Clock.fixed(Instant.parse("2026-10-25T02:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        RegisterPaymentService service = new RegisterPaymentService(contracts, payments, events, lateNight);

        assertThatThrownBy(() -> service.register(pay(1, "945.60", LocalDate.of(2026, 10, 25))))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
    }

    @Test
    @DisplayName("PAG-03 0007/CT-8: pagamento antes da liberação é recusado")
    void refusesDateBeforeRelease() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", LocalDate.of(2026, 9, 9))))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
    }

    @Test
    void acceptsPaymentOnTheReleaseDate() {
        onTimeContract();

        assertThat(serviceOn(TODAY).register(pay(1, "945.60", RELEASE)).getPaidOn())
                .isEqualTo(RELEASE);
    }

    @Test
    @DisplayName("PAG-04 0007/CT-9: parcela já paga é recusada")
    void refusesInstallmentAlreadyPaid() {
        onTimeContract();
        alreadyPaid(1);

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", TODAY)))
                .isInstanceOf(InstallmentAlreadyPaidException.class)
                .hasMessage("Parcela já paga");
        verify(payments, never()).save(any());
    }

    @Test
    @DisplayName("PAG-01 0007/CT-10: o pagamento aceito é entregue para virar evento")
    void publishesTheRegisteredPayment() {
        onTimeContract();

        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        verify(events).paymentRegistered(payment);
    }

    @Test
    void refusedPaymentPublishesNothing() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.59", TODAY)))
                .isInstanceOf(InvalidPaymentException.class);
        verifyNoInteractions(events);
    }

    @Test
    void unknownInstallmentIsRefused() {
        onTimeContract();

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(13, "945.60", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Pague antes a parcela 1");
    }

    @Test
    void unknownContractIsNotFound() {
        RegisterPaymentCommand command =
                new RegisterPaymentCommand(UUID.randomUUID(), 1, TODAY, new BigDecimal("945.60"), "operador");

        assertThatThrownBy(() -> serviceOn(TODAY).register(command))
                .isInstanceOf(ContractNotFoundForPaymentException.class)
                .hasMessage("Contrato não encontrado");
    }

    @Test
    void allPaidLeavesNothingToPay() {
        onTimeContract();
        Set<Integer> all = IntStream.rangeClosed(1, 12).boxed().collect(Collectors.toSet());
        when(payments.paidInstallments(CONTRACT_ID)).thenReturn(all);

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(12, "945.60", TODAY)))
                .isInstanceOf(InstallmentAlreadyPaidException.class);
    }
}
