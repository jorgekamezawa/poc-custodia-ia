package com.bancoexemplo.custody.payments.application.port;

import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.util.Set;
import java.util.UUID;

/** Where payments are kept; the source of which installments are paid. */
public interface PaymentRepository {

    Set<Integer> paidInstallments(UUID contractId);

    /**
     * Stores the payment.
     *
     * @throws com.bancoexemplo.custody.payments.domain.payment.exception.InstallmentAlreadyPaidException when it already has one
     */
    void save(Payment payment);
}
