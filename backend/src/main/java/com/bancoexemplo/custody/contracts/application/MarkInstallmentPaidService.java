package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MarkInstallmentPaidService {

    private final ContractRepository repository;

    public MarkInstallmentPaidService(ContractRepository repository) {
        this.repository = repository;
    }

    public void markPaid(UUID contractId, int installmentNumber, LocalDate paidOn) {
        repository.markInstallmentPaid(contractId, installmentNumber, paidOn);
    }
}
