package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.Cpf;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JdbcContractRepositoryTest {

    @Autowired
    private JdbcContractRepository repository;

    private static Contract contract(String number) {
        return new Contract(
                UUID.randomUUID(),
                new ContractNumber(number),
                "Cliente Exemplo",
                new Cpf("12345678909"),
                new ChassisNumber("9BWZZZ377VT004251"),
                "Hatch 1.0 2026",
                AmortizationSystem.PRICE,
                new BigDecimal("10000.00"),
                new BigDecimal("2.0000"),
                12,
                LocalDate.of(2027, 1, 10),
                LocalDate.of(2027, 2, 10),
                new BigDecimal("0.00"),
                new BigDecimal("26.95"),
                ContractStatus.ACTIVE,
                "operador",
                Instant.parse("2027-01-10T13:00:00Z"),
                installmentsOfTheExample());
    }

    /** The 12 installments of the spec example: 11 of R$ 945.60 and the last of R$ 945.55. */
    private static List<ContractInstallment> installmentsOfTheExample() {
        List<ContractInstallment> installments = new ArrayList<>();
        for (int number = 1; number <= 12; number++) {
            installments.add(new ContractInstallment(
                    number,
                    LocalDate.of(2027, 2, 10).plusMonths(number - 1L),
                    new BigDecimal(number == 12 ? "945.55" : "945.60"),
                    new BigDecimal("10.00"),
                    new BigDecimal("935.60"),
                    new BigDecimal("100.00")));
        }
        return installments;
    }

    @Test
    @DisplayName("CON-03 0005/CT-13: o contrato gravado volta igual, com as parcelas")
    void savedContractComesBackTheSame() {
        Contract saved = contract("CTR-REPO-" + UUID.randomUUID().toString().substring(0, 8));
        repository.save(saved);

        Contract loaded = repository.findById(saved.id()).orElseThrow();

        assertThat(loaded).isEqualTo(saved);
        assertThat(loaded.installments()).hasSize(12);
        assertThat(loaded.installments().get(11).amount()).isEqualByComparingTo("945.55");
        assertThat(loaded.installments())
                .extracting(ContractInstallment::number)
                .isSorted();
        assertThat(repository.existsByNumber(saved.number())).isTrue();
    }

    @Test
    void unknownContractIsEmpty() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
        assertThat(repository.existsByNumber(new ContractNumber("NAO-EXISTE"))).isFalse();
    }

    @Test
    @DisplayName("CON-01 0005/CT-14: o banco recusa um segundo contrato com o mesmo número")
    void databaseRefusesDuplicateNumber() {
        String number = "CTR-DUP-" + UUID.randomUUID().toString().substring(0, 8);
        repository.save(contract(number));

        assertThatThrownBy(() -> repository.save(contract(number)))
                .isInstanceOf(DuplicateContractNumberException.class);
    }
}
