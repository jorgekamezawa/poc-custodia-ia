package com.bancoexemplo.custody.contracts.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bancoexemplo.custody.contracts.ContractFixture;
import com.bancoexemplo.custody.contracts.application.exception.ContractNotFoundException;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import com.bancoexemplo.custody.contracts.domain.contract.Contract;
import com.bancoexemplo.custody.contracts.domain.contract.exception.InstallmentNotPayableException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkInstallmentPaidServiceTest {

    private static final LocalDate PAID_ON = LocalDate.of(2026, 10, 24);

    @Mock
    private ContractRepository repository;

    @InjectMocks
    private MarkInstallmentPaidService service;

    @Test
    void openInstallmentIsRecordedPaid() {
        Contract contract = ContractFixture.contract("CTR-0001");
        when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

        service.markPaid(contract.getId(), 1, PAID_ON);

        verify(repository).markInstallmentPaid(contract.getId(), 1, PAID_ON);
    }

    @Test
    void installmentTheContractRefusesIsNotRecorded() {
        Contract contract = ContractFixture.contract("CTR-0001");
        contract.payInstallment(1, PAID_ON);
        when(repository.findById(contract.getId())).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> service.markPaid(contract.getId(), 1, PAID_ON.plusDays(1)))
                .isInstanceOf(InstallmentNotPayableException.class)
                .hasMessage("Parcela 1 já está paga");
        verify(repository, never()).markInstallmentPaid(any(), anyInt(), any());
    }

    @Test
    void unknownContractIsNotFoundAndNothingIsRecorded() {
        UUID unknown = UUID.randomUUID();
        when(repository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.markPaid(unknown, 1, PAID_ON)).isInstanceOf(ContractNotFoundException.class);
        verify(repository, never()).markInstallmentPaid(any(), anyInt(), any());
    }
}
