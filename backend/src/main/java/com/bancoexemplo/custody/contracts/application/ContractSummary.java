package com.bancoexemplo.custody.contracts.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A contract as a line of the search result. */
public record ContractSummary(
        UUID id, String number, String customerName, String customerCpf, BigDecimal principal, Instant registeredAt) {}
