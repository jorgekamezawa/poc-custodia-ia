package com.bancoexemplo.custody.payments.adapter.out.events;

import com.bancoexemplo.custody.events.api.EventPublisher;
import com.bancoexemplo.custody.payments.application.port.PaymentEvents;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Publishes the registered payment on {@code custody.payment-registered}, keyed by contract. */
@Component
class PaymentEventsAdapter implements PaymentEvents {

    static final String TOPIC = "custody.payment-registered";

    private final EventPublisher publisher;

    PaymentEventsAdapter(EventPublisher publisher) {
        this.publisher = publisher;
    }

    record PaymentRegistered(UUID contractId, int installmentNumber, LocalDate paidOn, BigDecimal amount) {}

    @Override
    public void paymentRegistered(Payment payment) {
        publisher.publish(
                TOPIC,
                payment.getContractId().toString(),
                new PaymentRegistered(
                        payment.getContractId(),
                        payment.getInstallmentNumber(),
                        payment.getPaidOn(),
                        payment.getAmount()));
    }
}
