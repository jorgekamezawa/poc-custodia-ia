package com.bancoexemplo.custody.contracts.application;

import static org.mockito.Mockito.verify;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MarkInstallmentPaidServiceTest {

    @Mock
    private ContractRepository repository;

    @InjectMocks
    private MarkInstallmentPaidService service;

    @Test
    void recordsThePaymentDateOfTheInstallment() {
        UUID contractId = UUID.randomUUID();

        service.markPaid(contractId, 1, LocalDate.of(2026, 10, 24));

        verify(repository).markInstallmentPaid(contractId, 1, LocalDate.of(2026, 10, 24));
    }
}
