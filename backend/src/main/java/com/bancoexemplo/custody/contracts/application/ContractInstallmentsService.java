package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.api.ContractInstallments;
import com.bancoexemplo.custody.contracts.api.ContractSchedule;
import com.bancoexemplo.custody.contracts.application.port.ContractRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ContractInstallmentsService implements ContractInstallments {

    private final ContractRepository repository;

    public ContractInstallmentsService(ContractRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ContractSchedule> find(UUID contractId) {
        return repository
                .findById(contractId)
                .map(contract -> new ContractSchedule(
                        contract.releaseDate(),
                        contract.installments().stream()
                                .map(installment -> new ContractSchedule.ScheduledInstallment(
                                        installment.number(), installment.dueDate(), installment.amount()))
                                .toList()));
    }
}
