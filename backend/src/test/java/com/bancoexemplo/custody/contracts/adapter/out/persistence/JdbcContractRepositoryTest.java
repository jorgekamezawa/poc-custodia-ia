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
                List.of(
                        new ContractInstallment(
                                1,
                                LocalDate.of(2027, 2, 10),
                                new BigDecimal("945.60"),
                                new BigDecimal("200.00"),
                                new BigDecimal("745.60"),
                                new BigDecimal("9254.40")),
                        new ContractInstallment(
                                2,
                                LocalDate.of(2027, 3, 10),
                                new BigDecimal("945.60"),
                                new BigDecimal("185.09"),
                                new BigDecimal("760.51"),
                                new BigDecimal("8493.89"))));
    }

    @Test
    @DisplayName("CON-03 0005/CT-13: o contrato gravado volta igual, com as parcelas")
    void savedContractComesBackTheSame() {
        Contract saved = contract("CTR-REPO-" + UUID.randomUUID().toString().substring(0, 8));
        repository.save(saved);

        assertThat(repository.findById(saved.id())).contains(saved);
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
