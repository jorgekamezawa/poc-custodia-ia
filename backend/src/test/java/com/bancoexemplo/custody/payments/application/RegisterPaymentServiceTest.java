package com.bancoexemplo.custody.payments.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.payments.domain.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.InvalidPaymentException;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RegisterPaymentServiceTest {

    private static final String AMOUNT_MESSAGE = "Valor deve ser igual ao da parcela: R$ 945,60";
    private static final String DATE_RANGE_MESSAGE = "Data do pagamento deve ficar entre a liberação e hoje";

    private final PaymentFakes.Payments payments = new PaymentFakes.Payments();
    private final PaymentFakes.Events events = new PaymentFakes.Events();

    /** Service whose today, in Brasília, is the given date (15:00 UTC is midday there). */
    private RegisterPaymentService serviceOn(LocalDate today) {
        Clock clock = Clock.fixed(Instant.parse(today + "T15:00:00Z"), ZoneOffset.UTC);
        return new RegisterPaymentService(new PaymentFakes.Contracts(), payments, events, clock);
    }

    private static RegisterPaymentCommand pay(int installment, String amount, LocalDate paidOn) {
        return new RegisterPaymentCommand(
                PaymentFakes.Contracts.ID, installment, paidOn, new BigDecimal(amount), "operador");
    }

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);

    @Test
    @DisplayName("PAG-01 0007/CT-1: pagamento em dia da parcela mais antiga é aceito e guardado")
    void acceptsOnTimePayment() {
        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThat(payments.saved).containsExactly(payment);
        assertThat(payment.contractId()).isEqualTo(PaymentFakes.Contracts.ID);
        assertThat(payment.installmentNumber()).isEqualTo(1);
        assertThat(payment.paidOn()).isEqualTo(TODAY);
        assertThat(payment.amount()).isEqualByComparingTo("945.60");
        assertThat(payment.registeredBy()).isEqualTo("operador");
        assertThat(payment.registeredAt()).isEqualTo(Instant.parse("2026-10-24T15:00:00Z"));
    }

    @Test
    @DisplayName("PAG-01 0007/CT-2: parcela 2 com a 1 em aberto é recusada")
    void refusesSkippingTheOldestUnpaid() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(2, "945.60", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Pague antes a parcela 1");
        assertThat(payments.saved).isEmpty();
    }

    @Test
    void nextInstallmentIsAcceptedAfterTheFirst() {
        serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThat(serviceOn(TODAY).register(pay(2, "945.60", TODAY)).installmentNumber())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("PAG-02 0007/CT-3: um centavo a menos é recusado")
    void refusesOneCentLess() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.59", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(AMOUNT_MESSAGE);
    }

    @Test
    @DisplayName("PAG-02 0007/CT-4: um centavo a mais é recusado")
    void refusesOneCentMore() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.61", TODAY)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(AMOUNT_MESSAGE);
    }

    @Test
    @DisplayName("PAG-05 0008/CT-5: pagamento atrasado com multa e juros é aceito e guarda os dois")
    void acceptsLatePaymentWithCharges() {
        Payment payment = serviceOn(TODAY).register(payLate("968.92"));

        assertThat(payment.amount()).isEqualByComparingTo("968.92");
        assertThat(payment.lateFee()).isEqualByComparingTo("18.91");
        assertThat(payment.defaultInterest()).isEqualByComparingTo("4.41");
        assertThat(payments.saved).containsExactly(payment);
    }

    @Test
    @DisplayName("PAG-02 0008/CT-6: pagamento atrasado sem multa e juros é recusado com o valor devido")
    void refusesLatePaymentWithoutCharges() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(payLate("945.60")))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Valor deve ser igual ao da parcela com multa e juros de mora: R$ 968,92");
    }

    @Test
    @DisplayName("PAG-06 0008/CT-7: a simulação traz parcela, dias, multa, juros e total")
    void quotesTheAmountDue() {
        PaymentQuote quote = serviceOn(TODAY).quote(PaymentFakes.Contracts.LATE_ID, 1, TODAY);

        assertThat(quote.installmentNumber()).isEqualTo(1);
        assertThat(quote.installmentAmount()).isEqualByComparingTo("945.60");
        assertThat(quote.daysLate()).isEqualTo(14);
        assertThat(quote.lateFee()).isEqualByComparingTo("18.91");
        assertThat(quote.defaultInterest()).isEqualByComparingTo("4.41");
        assertThat(quote.total()).isEqualByComparingTo("968.92");
    }

    @Test
    void quoteAppliesTheSameRefusals() {
        RegisterPaymentService service = serviceOn(TODAY);

        assertThatThrownBy(() -> service.quote(PaymentFakes.Contracts.ID, 2, TODAY))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Pague antes a parcela 1");
        service.register(pay(1, "945.60", TODAY));
        assertThatThrownBy(() -> service.quote(PaymentFakes.Contracts.ID, 1, TODAY))
                .isInstanceOf(InstallmentAlreadyPaidException.class);
        assertThatThrownBy(() -> service.quote(PaymentFakes.Contracts.ID, 2, TODAY.plusDays(1)))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
        assertThatThrownBy(() -> service.quote(UUID.randomUUID(), 1, TODAY))
                .isInstanceOf(ContractNotFoundForPaymentException.class);
    }

    @Test
    void onTimePaymentHasNoCharges() {
        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThat(payment.lateFee()).isEqualByComparingTo("0.00");
        assertThat(payment.defaultInterest()).isEqualByComparingTo("0.00");
    }

    private static RegisterPaymentCommand payLate(String amount) {
        return new RegisterPaymentCommand(PaymentFakes.Contracts.LATE_ID, 1, TODAY, new BigDecimal(amount), "operador");
    }

    @Test
    @DisplayName("PAG-03 0007/CT-6: pagamento no dia do vencimento é aceito")
    void acceptsPaymentOnTheDueDate() {
        LocalDate due = LocalDate.of(2026, 11, 10);

        assertThat(serviceOn(due).register(pay(1, "945.60", due)).paidOn()).isEqualTo(due);
    }

    @Test
    @DisplayName("PAG-03 0007/CT-7: pagamento com data de amanhã é recusado")
    void refusesFutureDate() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", TODAY.plusDays(1))))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
    }

    @Test
    @DisplayName("PAG-03 0007/CT-8: pagamento antes da liberação é recusado")
    void refusesDateBeforeRelease() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", LocalDate.of(2026, 9, 9))))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage(DATE_RANGE_MESSAGE);
    }

    @Test
    void acceptsPaymentOnTheReleaseDate() {
        assertThat(serviceOn(TODAY)
                        .register(pay(1, "945.60", PaymentFakes.Contracts.RELEASE))
                        .paidOn())
                .isEqualTo(PaymentFakes.Contracts.RELEASE);
    }

    @Test
    @DisplayName("PAG-04 0007/CT-9: parcela já paga é recusada")
    void refusesInstallmentAlreadyPaid() {
        serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.60", TODAY)))
                .isInstanceOf(InstallmentAlreadyPaidException.class)
                .hasMessage("Parcela já paga");
        assertThat(payments.saved).hasSize(1);
    }

    @Test
    @DisplayName("PAG-01 0007/CT-10: o pagamento aceito é entregue para virar evento")
    void publishesTheRegisteredPayment() {
        Payment payment = serviceOn(TODAY).register(pay(1, "945.60", TODAY));

        assertThat(events.published).containsExactly(payment);
    }

    @Test
    void refusedPaymentPublishesNothing() {
        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(1, "945.59", TODAY)));

        assertThat(events.published).isEmpty();
    }

    @Test
    void unknownInstallmentIsRefused() {
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
        for (int n = 1; n <= 12; n++) {
            serviceOn(TODAY).register(pay(n, "945.60", TODAY));
        }

        assertThatThrownBy(() -> serviceOn(TODAY).register(pay(12, "945.60", TODAY)))
                .isInstanceOf(InstallmentAlreadyPaidException.class);
    }
}
