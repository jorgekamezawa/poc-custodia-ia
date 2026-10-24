package com.bancoexemplo.custody.payments.application.port;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** The contract terms a payment is checked against. */
public interface ContractToPay {

    Optional<ContractTerms> find(UUID contractId);

    /** Release date and installments of the contract. */
    record ContractTerms(LocalDate releaseDate, List<InstallmentTerms> installments) {

        public ContractTerms {
            installments = List.copyOf(installments);
        }
    }

    /** An installment as far as paying it goes. */
    record InstallmentTerms(int number, LocalDate dueDate, BigDecimal amount) {}
}
