package com.bancoexemplo.custody.payments.adapter.out.contracts;

import com.bancoexemplo.custody.contracts.api.ContractInstallments;
import com.bancoexemplo.custody.payments.application.port.ContractToPay;
import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentTerms;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Reads the contract terms through the contracts module api. */
@Component
class ContractsAdapter implements ContractToPay {

    private final ContractInstallments contracts;

    ContractsAdapter(ContractInstallments contracts) {
        this.contracts = contracts;
    }

    @Override
    public Optional<ContractTerms> find(UUID contractId) {
        return contracts
                .find(contractId)
                .map(schedule -> new ContractTerms(
                        schedule.releaseDate(),
                        schedule.installments().stream()
                                .map(installment -> new InstallmentTerms(
                                        installment.number(), installment.dueDate(), installment.amount()))
                                .toList()));
    }
}
