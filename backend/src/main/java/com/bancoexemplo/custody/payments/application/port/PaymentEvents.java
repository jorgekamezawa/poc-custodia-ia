package com.bancoexemplo.custody.payments.application.port;

import com.bancoexemplo.custody.payments.domain.payment.Payment;

/** Tells other modules a payment was registered, in the same transaction. */
public interface PaymentEvents {

    void paymentRegistered(Payment payment);
}
