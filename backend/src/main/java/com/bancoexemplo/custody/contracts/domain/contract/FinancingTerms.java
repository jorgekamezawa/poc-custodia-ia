package com.bancoexemplo.custody.contracts.domain.contract;

import com.bancoexemplo.custody.contracts.domain.contract.exception.InvalidContractException;
import java.math.BigDecimal;
import java.time.LocalDate;

/** The financing terms of the signed contract, all required; their limits are the calculator's. */
public record FinancingTerms(
        AmortizationSystem amortizationSystem,
        BigDecimal principal,
        BigDecimal monthlyRatePercent,
        Integer termMonths,
        LocalDate releaseDate,
        LocalDate firstDueDate,
        BigDecimal upfrontCosts) {

    public FinancingTerms {
        required(amortizationSystem, "Sistema de amortização");
        required(principal, "Valor financiado");
        required(monthlyRatePercent, "Taxa ao mês");
        required(termMonths, "Prazo");
        required(releaseDate, "Data de liberação");
        required(firstDueDate, "Primeiro vencimento");
        required(upfrontCosts, "Custos pagos antecipadamente");
    }

    private static void required(Object value, String label) {
        if (value == null) {
            throw new InvalidContractException(label + " é obrigatório");
        }
    }
}
