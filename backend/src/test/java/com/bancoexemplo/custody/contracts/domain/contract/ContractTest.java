package com.bancoexemplo.custody.contracts.domain.contract;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.contracts.ContractFixture;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ContractTest {

    private static final Instant NOW = Instant.parse("2027-01-10T13:00:00Z");

    private static Contract registered() {
        return Contract.create(
                new ContractNumber("CTR-0001"),
                new CustomerName("Cliente Exemplo"),
                new Cpf("12345678909"),
                new ChassisNumber("9BWZZZ377VT004251"),
                new VehicleDescription("Hatch 1.0 2026"),
                ContractFixture.terms(LocalDate.of(2027, 1, 10), LocalDate.of(2027, 2, 10)),
                new Financing(
                        List.of(new CalculatedInstallment(
                                1,
                                LocalDate.of(2027, 2, 10),
                                new BigDecimal("945.60"),
                                new BigDecimal("200.00"),
                                new BigDecimal("745.60"),
                                new BigDecimal("9254.40"))),
                        new BigDecimal("26.95")),
                "operador",
                NOW);
    }

    @Test
    void registeredContractIsActiveWithTheCalculatedScheduleUnpaid() {
        Contract contract = registered();

        assertThat(contract.getId()).isNotNull();
        assertThat(contract.getStatus()).isEqualTo(ContractStatus.ACTIVE);
        assertThat(contract.getAnnualCetPercent()).isEqualByComparingTo("26.95");
        assertThat(contract.getRegisteredBy()).isEqualTo("operador");
        assertThat(contract.getRegisteredAt()).isEqualTo(NOW);
        ContractInstallment first = contract.getInstallments().get(0);
        assertThat(first.getNumber()).isEqualTo(1);
        assertThat(first.getDueDate()).isEqualTo(LocalDate.of(2027, 2, 10));
        assertThat(first.getAmount()).isEqualByComparingTo("945.60");
        assertThat(first.getInterest()).isEqualByComparingTo("200.00");
        assertThat(first.getAmortization()).isEqualByComparingTo("745.60");
        assertThat(first.getBalanceAfter()).isEqualByComparingTo("9254.40");
        assertThat(first.isPaid()).isFalse();
    }

    @Test
    void eachRegistrationIsAnotherContract() {
        assertThat(registered()).isNotEqualTo(registered());
    }

    @Test
    void contractsAreTheSameByIdWhateverElseDiffers() {
        Contract stored = ContractFixture.contract("CTR-0001");
        Contract sameId = Contract.reconstruct(
                stored.getId(),
                new ContractNumber("CTR-OUTRO"),
                stored.getCustomerName(),
                stored.getCustomerCpf(),
                stored.getChassisNumber(),
                stored.getVehicleDescription(),
                stored.getTerms(),
                stored.getAnnualCetPercent(),
                stored.getStatus(),
                stored.getRegisteredBy(),
                stored.getRegisteredAt(),
                stored.getInstallments());

        assertThat(sameId).isEqualTo(stored).hasSameHashCodeAs(stored);
        assertThat(stored).isNotEqualTo(ContractFixture.contract("CTR-0001"));
    }

    @Test
    void installmentsAreTheSameByNumberWithinTheContract() {
        ContractInstallment first =
                ContractFixture.contract("CTR-0001").getInstallments().get(0);
        ContractInstallment firstPaid = ContractInstallment.reconstruct(
                1,
                first.getDueDate(),
                first.getAmount(),
                first.getInterest(),
                first.getAmortization(),
                first.getBalanceAfter(),
                LocalDate.of(2027, 2, 1));

        assertThat(firstPaid).isEqualTo(first).hasSameHashCodeAs(first);
        assertThat(firstPaid)
                .isNotEqualTo(
                        ContractFixture.contract("CTR-0001").getInstallments().get(1));
    }
}
