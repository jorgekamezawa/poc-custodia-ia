package com.bancoexemplo.custody.contracts.application.port;

import com.bancoexemplo.custody.contracts.domain.contract.Financing;
import com.bancoexemplo.custody.contracts.domain.contract.FinancingTerms;

/** Calculates the schedule and the CET of a contract. */
public interface FinancingCalculator {

    /**
     * Calculates the schedule and the CET for the terms.
     *
     * @throws com.bancoexemplo.custody.contracts.domain.contract.InvalidContractException when a term is outside the limits
     */
    Financing calculate(FinancingTerms terms);
}
