package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.domain.Contract;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import com.bancoexemplo.custody.contracts.domain.InstallmentSituation;
import java.math.BigDecimal;
import java.util.List;

/** A contract as consulted today: its data, outstanding balance and each installment's situation. */
public record ContractView(Contract contract, BigDecimal outstandingBalance, List<InstallmentView> installments) {

    public ContractView {
        installments = List.copyOf(installments);
    }

    /** An installment with its situation today. */
    public record InstallmentView(ContractInstallment installment, InstallmentSituation situation) {}
}
