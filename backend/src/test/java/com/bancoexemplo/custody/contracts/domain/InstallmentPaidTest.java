package com.bancoexemplo.custody.contracts.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.contracts.ContractFixture;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstallmentPaidTest {

    @Test
    @DisplayName("CON-05 0007/CT-13: parcela paga aparece paga mesmo com o vencimento já passado")
    void paidInstallmentIsPaidEvenAfterTheDueDate() {
        ContractInstallment paid = new ContractInstallment(
                1,
                LocalDate.of(2026, 10, 10),
                new BigDecimal("945.60"),
                new BigDecimal("200.00"),
                new BigDecimal("745.60"),
                new BigDecimal("9254.40"),
                LocalDate.of(2026, 10, 9));

        assertThat(InstallmentSituation.of(paid, LocalDate.of(2026, 10, 24))).isEqualTo(InstallmentSituation.PAID);
    }

    @Test
    void unpaidInstallmentFollowsTheDueDate() {
        ContractInstallment open =
                ContractFixture.contract("CTR-0001").installments().get(0);

        assertThat(open.paidOn()).isNull();
        assertThat(InstallmentSituation.of(open, open.dueDate())).isEqualTo(InstallmentSituation.OPEN);
        assertThat(InstallmentSituation.of(open, open.dueDate().plusDays(1))).isEqualTo(InstallmentSituation.OVERDUE);
    }

    @Test
    void balanceIsTheOneAfterTheLastPaidInstallment() {
        Contract contract = ContractFixture.contract("CTR-0001");
        Contract withFirstPaid = contract.withInstallmentPaid(1, LocalDate.of(2027, 2, 1));

        assertThat(withFirstPaid.installments().get(0).paidOn()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(withFirstPaid.outstandingBalance())
                .isEqualByComparingTo(contract.installments().get(0).balanceAfter());
        assertThat(contract.outstandingBalance()).isEqualByComparingTo("10000.00");
    }
}
