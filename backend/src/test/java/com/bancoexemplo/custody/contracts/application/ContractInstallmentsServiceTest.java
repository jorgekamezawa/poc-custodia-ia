package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.api.ContractSchedule;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractInstallmentsServiceTest {

    @Mock
    private ContractRepository repository;

    @InjectMocks
    private ContractInstallmentsService service;

    @Test
    void exposesReleaseDateAndInstallments() {
        Contract contract = ContractFixture.contract("CTR-0001");
        when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

        ContractSchedule schedule = service.find(contract.getId()).orElseThrow();

        assertThat(schedule.releaseDate()).isEqualTo(contract.getTerms().releaseDate());
        assertThat(schedule.installments()).hasSize(12);
        assertThat(schedule.installments().get(0))
                .isEqualTo(new ContractSchedule.ScheduledInstallment(
                        1,
                        LocalDate.of(2027, 2, 10),
                        contract.getInstallments().get(0).getAmount()));
    }

    @Test
    void unknownContractIsEmpty() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThat(service.find(unknown)).isEmpty();
    }
}
