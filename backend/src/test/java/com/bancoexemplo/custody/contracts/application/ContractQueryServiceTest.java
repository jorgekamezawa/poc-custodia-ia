package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.InstallmentSituation;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractQueryServiceTest {

    private final InMemoryContractRepository repository = new InMemoryContractRepository();
    private final ContractQueryService service =
            new ContractQueryService(repository, Clock.fixed(Instant.parse("2026-10-24T15:00:00Z"), ZoneOffset.UTC));

    @Test
    void viewHasEachInstallmentSituationAndTheBalance() {
        Contract contract = ContractFixture.contract(
                "CTR-0001", "12345678909", LocalDate.of(2026, 10, 10), Instant.parse("2026-09-10T13:00:00Z"));
        repository.save(contract);

        ContractView view = service.view(contract.id());

        assertThat(view.outstandingBalance()).isEqualByComparingTo("10000.00");
        assertThat(view.installments().get(0).situation()).isEqualTo(InstallmentSituation.OVERDUE);
        assertThat(view.installments().get(1).situation()).isEqualTo(InstallmentSituation.OPEN);
        assertThat(view.installments().get(0).installment().number()).isEqualTo(1);
    }

    @Test
    void unknownContractIsNotFound() {
        assertThatThrownBy(() -> service.view(UUID.randomUUID()))
                .isInstanceOf(ContractNotFoundException.class)
                .hasMessage("Contrato não encontrado");
    }

    @Test
    void elevenDigitsWithPunctuationSearchTheCpf() {
        Contract contract = ContractFixture.contract(
                "CTR-0001", "52998224725", LocalDate.of(2027, 2, 10), Instant.parse("2027-01-10T13:00:00Z"));
        repository.save(contract);

        assertThat(service.search("529.982.247-25")).containsExactly(ContractSummary.of(contract));
        assertThat(repository.lastSearch).isEqualTo("cpf:52998224725");
    }

    @Test
    void otherTextSearchesTheNumberPrefix() {
        repository.save(ContractFixture.contract("CTR-0001"));

        assertThat(service.search(" ctr-00 ")).singleElement().satisfies(summary -> {
            assertThat(summary.number()).isEqualTo("CTR-0001");
            assertThat(summary.customerName()).isEqualTo("Cliente Exemplo");
            assertThat(summary.customerCpf()).isEqualTo("12345678909");
            assertThat(summary.principal()).isEqualByComparingTo("10000.00");
            assertThat(summary.registeredAt()).isEqualTo(Instant.parse("2027-01-10T13:00:00Z"));
        });
        assertThat(repository.lastSearch).isEqualTo("number:ctr-00");
    }

    @Test
    void elevenCharactersWithLettersSearchTheNumber() {
        service.search("CTR12345678");

        assertThat(repository.lastSearch).isEqualTo("number:CTR12345678");
    }

    @Test
    void noTextListsTheLatest() {
        repository.save(ContractFixture.contract("CTR-0001"));

        assertThat(service.search(null)).hasSize(1);
        assertThat(repository.lastSearch).isEqualTo("latest");

        service.search("   ");
        assertThat(repository.lastSearch).isEqualTo("latest");
    }
}
