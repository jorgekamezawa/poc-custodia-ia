package com.bancoexemplo.custody.events.api;

/** Records an event to be published to other modules, inside the caller's transaction. */
public interface EventPublisher {

    /**
     * Stores the event in the outbox; it reaches Kafka only if the caller's transaction commits.
     *
     * @param key messages with the same key keep their order, so use the aggregate id (e.g. the contract)
     * @param payload serialized to JSON, with {@code eventId} and {@code occurredAt} added
     */
    void publish(String topic, String key, Object payload);
}
