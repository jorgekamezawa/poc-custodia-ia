package com.bancoexemplo.custody.contracts.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.TestcontainersConfiguration;
import com.bancoexemplo.custody.contracts.application.ContractSummary;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.AmortizationSystem;
import com.bancoexemplo.custody.contracts.domain.ChassisNumber;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.ContractNumber;
import com.bancoexemplo.custody.contracts.domain.ContractStatus;
import com.bancoexemplo.custody.contracts.domain.Cpf;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.InstallmentNotPayableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JdbcContractRepositoryTest {

    private static final Instant REGISTERED_AT = Instant.parse("2027-01-10T13:00:00Z");

    @Autowired
    private ContractRepository repository;

    private static Contract contract(String number) {
        return contract(number, "12345678909", REGISTERED_AT);
    }

    private static Contract contract(String number, String cpf, Instant registeredAt) {
        return new Contract(
                UUID.randomUUID(),
                new ContractNumber(number),
                "Cliente Exemplo",
                new Cpf(cpf),
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
                registeredAt,
                installmentsOfTheExample());
    }

    /** The 12 installments of the spec example: 11 of R$ 945.60, the last of R$ 945.55, R$ 9,254.40 after the first. */
    private static List<ContractInstallment> installmentsOfTheExample() {
        List<ContractInstallment> installments = new ArrayList<>();
        for (int number = 1; number <= 12; number++) {
            installments.add(new ContractInstallment(
                    number,
                    LocalDate.of(2027, 2, 10).plusMonths(number - 1L),
                    new BigDecimal(number == 12 ? "945.55" : "945.60"),
                    new BigDecimal("10.00"),
                    new BigDecimal("935.60"),
                    new BigDecimal(number == 1 ? "9254.40" : "100.00")));
        }
        return installments;
    }

    /** A number no other test uses, so searches see only this test's contracts. */
    private static String uniqueNumber(String prefix) {
        return prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    /** A valid CPF no other test uses, so a search by it sees only this test's contracts. */
    private static String uniqueCpf() {
        StringBuilder digits = new StringBuilder();
        digits.append(1 + ThreadLocalRandom.current().nextInt(9));
        for (int i = 1; i < 9; i++) {
            digits.append(ThreadLocalRandom.current().nextInt(10));
        }
        for (int length = 9; length <= 10; length++) {
            int sum = 0;
            for (int i = 0; i < length; i++) {
                sum += (digits.charAt(i) - '0') * (length + 1 - i);
            }
            digits.append(sum * 10 % 11 % 10);
        }
        return digits.toString();
    }

    private static ContractSummary summaryOf(Contract contract) {
        return new ContractSummary(
                contract.id(),
                contract.number().value(),
                contract.customerName(),
                contract.customerCpf().digits(),
                contract.principal(),
                contract.registeredAt());
    }

    @Test
    @DisplayName("CON-03 0005/CT-13: o contrato gravado volta igual, com as parcelas")
    void savedContractComesBackTheSame() {
        Contract saved = contract(uniqueNumber("CTR-REPO-"));
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
        String number = uniqueNumber("CTR-DUP-");
        repository.save(contract(number));

        assertThatThrownBy(() -> repository.save(contract(number)))
                .isInstanceOf(DuplicateContractNumberException.class);
    }

    @Test
    void latestComeNewestFirstUpToTheLimit() {
        Contract older = contract(uniqueNumber("CTR-NEW-"), "12345678909", Instant.parse("2999-01-01T00:00:01Z"));
        Contract newer = contract(uniqueNumber("CTR-NEW-"), "12345678909", Instant.parse("2999-01-01T00:00:02Z"));
        repository.save(older);
        repository.save(newer);

        assertThat(repository.findLatest(2)).containsExactly(summaryOf(newer), summaryOf(older));
        assertThat(repository.findLatest(1)).containsExactly(summaryOf(newer));
    }

    @Test
    void cpfSearchFindsOnlyThatCustomerNewestFirst() {
        String cpf = uniqueCpf();
        Contract first = contract(uniqueNumber("CTR-CPF-"), cpf, Instant.parse("2027-01-10T13:00:00Z"));
        Contract second = contract(uniqueNumber("CTR-CPF-"), cpf, Instant.parse("2027-01-11T13:00:00Z"));
        repository.save(first);
        repository.save(second);
        repository.save(contract(uniqueNumber("CTR-CPF-"), uniqueCpf(), REGISTERED_AT));

        assertThat(repository.findByCpf(cpf, 50)).containsExactly(summaryOf(second), summaryOf(first));
        assertThat(repository.findByCpf(cpf, 1)).containsExactly(summaryOf(second));
    }

    @Test
    void numberPrefixIgnoresCaseAndTreatsWildcardsAsText() {
        String number = uniqueNumber("PFX-");
        Contract contract = contract(number);
        repository.save(contract);

        assertThat(repository.findByNumberPrefix(number.substring(0, 6).toLowerCase(Locale.ROOT), 50))
                .containsExactly(summaryOf(contract));
        assertThat(repository.findByNumberPrefix(number.substring(0, 5) + "_", 50))
                .isEmpty();
        assertThat(repository.findByNumberPrefix(number.substring(0, 5) + "%", 50))
                .isEmpty();
    }

    @Test
    @DisplayName("CON-06 0007/CT-11: o contrato marca a parcela paga e o saldo cai para o da parcela")
    void markedInstallmentComesBackPaidWithTheBalanceAfterIt() {
        Contract contract = contract(uniqueNumber("CTR-PAG-"));
        repository.save(contract);
        assertThat(repository.findById(contract.id()).orElseThrow().outstandingBalance())
                .isEqualByComparingTo("10000.00");

        repository.markInstallmentPaid(contract.id(), 1, LocalDate.of(2026, 10, 24));

        Contract updated = repository.findById(contract.id()).orElseThrow();
        assertThat(updated.installments().get(0).paidOn()).isEqualTo(LocalDate.of(2026, 10, 24));
        assertThat(updated.installments().get(1).paidOn()).isNull();
        assertThat(updated.outstandingBalance()).isEqualByComparingTo("9254.40");
    }

    @Test
    void installmentAlreadyPaidKeepsItsDateAndIsRefused() {
        Contract contract = contract(uniqueNumber("CTR-PAG-"));
        repository.save(contract);
        repository.markInstallmentPaid(contract.id(), 1, LocalDate.of(2026, 10, 24));

        assertThatThrownBy(() -> repository.markInstallmentPaid(contract.id(), 1, LocalDate.of(2026, 10, 25)))
                .isInstanceOf(InstallmentNotPayableException.class);
        assertThat(repository
                        .findById(contract.id())
                        .orElseThrow()
                        .installments()
                        .get(0)
                        .paidOn())
                .isEqualTo(LocalDate.of(2026, 10, 24));
    }
}
