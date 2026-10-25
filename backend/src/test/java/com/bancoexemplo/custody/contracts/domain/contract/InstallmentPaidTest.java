package com.bancoexemplo.custody.contracts.domain.contract;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InstallmentNotPayableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstallmentPaidTest {

    @Test
    @DisplayName("CON-05 0007/CT-13: parcela paga aparece paga mesmo com o vencimento já passado")
    void paidInstallmentIsPaidEvenAfterTheDueDate() {
        ContractInstallment paid = ContractInstallment.reconstruct(
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
                ContractFixture.contract("CTR-0001").getInstallments().get(0);

        assertThat(open.isPaid()).isFalse();
        assertThat(InstallmentSituation.of(open, open.getDueDate())).isEqualTo(InstallmentSituation.OPEN);
        assertThat(InstallmentSituation.of(open, open.getDueDate().plusDays(1)))
                .isEqualTo(InstallmentSituation.OVERDUE);
    }

    @Test
    void balanceIsTheOneAfterTheLastPaidInstallment() {
        Contract contract = ContractFixture.contract("CTR-0001");
        assertThat(contract.outstandingBalance()).isEqualByComparingTo("10000.00");

        contract.payInstallment(1, LocalDate.of(2027, 2, 1));

        assertThat(contract.getInstallments().get(0).getPaidOn()).isEqualTo(LocalDate.of(2027, 2, 1));
        assertThat(contract.getInstallments().get(1).getPaidOn()).isNull();
        assertThat(contract.outstandingBalance())
                .isEqualByComparingTo(contract.getInstallments().get(0).getBalanceAfter());
    }

    @Test
    void installmentNotInTheScheduleIsRefused() {
        Contract contract = ContractFixture.contract("CTR-0001");

        assertThatThrownBy(() -> contract.payInstallment(13, LocalDate.of(2027, 2, 1)))
                .isInstanceOf(InstallmentNotPayableException.class)
                .hasMessage("Parcela 13 não existe no contrato");
    }

    @Test
    void installmentAlreadyPaidIsRefused() {
        Contract contract = ContractFixture.contract("CTR-0001");
        contract.payInstallment(1, LocalDate.of(2027, 2, 1));

        assertThatThrownBy(() -> contract.payInstallment(1, LocalDate.of(2027, 2, 5)))
                .isInstanceOf(InstallmentNotPayableException.class)
                .hasMessage("Parcela 1 já está paga");
        assertThat(contract.getInstallments().get(0).getPaidOn()).isEqualTo(LocalDate.of(2027, 2, 1));
    }
}
