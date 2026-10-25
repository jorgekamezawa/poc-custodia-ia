package com.bancoexemplo.custody.payments.application.port;

import com.bancoexemplo.custody.payments.domain.payment.ContractTerms;
import java.util.Optional;
import java.util.UUID;

/** The contract terms a payment is checked against. */
public interface ContractToPay {

    Optional<ContractTerms> find(UUID contractId);
}
