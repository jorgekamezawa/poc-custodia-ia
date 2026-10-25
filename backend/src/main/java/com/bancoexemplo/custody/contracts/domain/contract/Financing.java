package com.bancoexemplo.custody.contracts.domain.contract;

import java.math.BigDecimal;
import java.util.List;

/** Schedule and CET calculated for the financing terms of a contract. */
public record Financing(List<CalculatedInstallment> installments, BigDecimal annualCetPercent) {

    public Financing {
        installments = List.copyOf(installments);
    }
}
