package com.bancoexemplo.custody.contracts.application.port;

import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import java.math.BigDecimal;
import java.util.List;

/** Schedule and CET calculated for a contract. */
public record Financing(List<ContractInstallment> installments, BigDecimal annualCetPercent) {

    public Financing {
        installments = List.copyOf(installments);
    }
}
