package com.bancoexemplo.custody.payments.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 24);
    private static final UUID CONTRACT_ID = UUID.fromString("00000000-0000-4000-8000-000000000001");

    /** Released on 10/09/2026, one installment of R$ 945.60 due on 10/11/2026. */
    private static final ContractTerms TERMS = new ContractTerms(
            LocalDate.of(2026, 9, 10),
            List.of(new InstallmentTerms(1, LocalDate.of(2026, 11, 10), new BigDecimal("945.60"))));

    private static Payment pay(Integer installment, LocalDate paidOn, String amount) {
        return Payment.create(
                CONTRACT_ID,
                TERMS,
                Set.of(),
                installment,
                paidOn,
                amount == null ? null : new BigDecimal(amount),
                "operador",
                TODAY,
                Instant.parse("2026-10-24T15:00:00Z"));
    }

    @Test
    void registeredPaymentKeepsWhatWasPaidAndHasItsOwnId() {
        Payment payment = pay(1, TODAY, "945.60");

        assertThat(payment.getId()).isNotNull();
        assertThat(payment.getContractId()).isEqualTo(CONTRACT_ID);
        assertThat(payment.getInstallmentNumber()).isEqualTo(1);
        assertThat(payment.getAmount()).isEqualByComparingTo("945.60");
    }

    @Test
    void missingInstallmentIsRefused() {
        assertThatThrownBy(() -> pay(null, TODAY, "945.60"))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Parcela é obrigatório");
    }

    @Test
    void missingDateIsRefused() {
        assertThatThrownBy(() -> pay(1, null, "945.60"))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Data do pagamento é obrigatório");
    }

    @Test
    void missingAmountIsRefused() {
        assertThatThrownBy(() -> pay(1, TODAY, null))
                .isInstanceOf(InvalidPaymentException.class)
                .hasMessage("Valor é obrigatório");
    }

    @Test
    void eachRegistrationIsAnotherPayment() {
        assertThat(pay(1, TODAY, "945.60")).isNotEqualTo(pay(1, TODAY, "945.60"));
    }
}
