package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.api.ContractSchedule;
import com.bancoexemplo.custody.contracts.domain.Contract;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ContractInstallmentsServiceTest {

    private final InMemoryContractRepository repository = new InMemoryContractRepository();
    private final ContractInstallmentsService service = new ContractInstallmentsService(repository);

    @Test
    void exposesReleaseDateAndInstallments() {
        Contract contract = ContractFixture.contract("CTR-0001");
        repository.save(contract);

        ContractSchedule schedule = service.find(contract.id()).orElseThrow();

        assertThat(schedule.releaseDate()).isEqualTo(contract.releaseDate());
        assertThat(schedule.installments()).hasSize(12);
        assertThat(schedule.installments().get(0))
                .isEqualTo(new ContractSchedule.ScheduledInstallment(
                        1,
                        LocalDate.of(2027, 2, 10),
                        contract.installments().get(0).amount()));
    }

    @Test
    void unknownContractIsEmpty() {
        assertThat(service.find(UUID.randomUUID())).isEmpty();
    }
}
