package com.bancoexemplo.custody.events.api;

import java.util.UUID;

/** Remembers which events each consumer already applied, so a redelivered event has no second effect. */
public interface ProcessedEvents {

    /**
     * Marks the event as applied by the consumer, in the caller's transaction.
     *
     * @return true the first time; false when the consumer already applied it
     */
    boolean markProcessed(UUID eventId, String consumer);
}
