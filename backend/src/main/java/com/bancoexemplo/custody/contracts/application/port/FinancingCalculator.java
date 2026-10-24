package com.bancoexemplo.custody.contracts.application.port;

/** Calculates the schedule and the CET of a contract. */
public interface FinancingCalculator {

    /**
     * Calculates the schedule and the CET for the terms.
     *
     * @throws com.bancoexemplo.custody.contracts.domain.InvalidContractException when a term is outside the limits
     */
    Financing calculate(FinancingRequest request);
}
