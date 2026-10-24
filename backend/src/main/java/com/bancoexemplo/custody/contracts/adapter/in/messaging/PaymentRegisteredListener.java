package com.bancoexemplo.custody.contracts.adapter.in.messaging;

import com.bancoexemplo.custody.contracts.application.MarkInstallmentPaidService;
import com.bancoexemplo.custody.events.api.ProcessedEvents;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

/** Marks the installment paid when the payments module reports a payment, once per event. */
@Component
class PaymentRegisteredListener {

    static final String CONSUMER = "contracts";

    private final ProcessedEvents processedEvents;
    private final MarkInstallmentPaidService markInstallmentPaid;
    private final JsonMapper json;

    PaymentRegisteredListener(
            ProcessedEvents processedEvents, MarkInstallmentPaidService markInstallmentPaid, JsonMapper json) {
        this.processedEvents = processedEvents;
        this.markInstallmentPaid = markInstallmentPaid;
        this.json = json;
    }

    record PaymentRegistered(UUID eventId, UUID contractId, int installmentNumber, LocalDate paidOn) {}

    @KafkaListener(topics = "custody.payment-registered", groupId = CONSUMER)
    @Transactional
    public void on(String message) {
        PaymentRegistered event = json.readValue(message, PaymentRegistered.class);
        if (processedEvents.markProcessed(event.eventId(), CONSUMER)) {
            markInstallmentPaid.markPaid(event.contractId(), event.installmentNumber(), event.paidOn());
        }
    }
}
