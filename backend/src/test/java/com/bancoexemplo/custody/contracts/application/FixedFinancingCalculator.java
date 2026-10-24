package com.bancoexemplo.custody.contracts.application;

import com.bancoexemplo.custody.contracts.application.port.Financing;
import com.bancoexemplo.custody.contracts.application.port.FinancingCalculator;
import com.bancoexemplo.custody.contracts.application.port.FinancingRequest;
import com.bancoexemplo.custody.contracts.domain.ContractInstallment;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Answers a fixed 12-installment schedule with CET 26.95, recording the last request. */
class FixedFinancingCalculator implements FinancingCalculator {

    FinancingRequest lastRequest;

    @Override
    public Financing calculate(FinancingRequest request) {
        lastRequest = request;
        List<ContractInstallment> installments = new ArrayList<>();
        for (int number = 1; number <= 12; number++) {
            installments.add(new ContractInstallment(
                    number,
                    request.firstDueDate().plusMonths(number - 1L),
                    new BigDecimal(number == 12 ? "945.55" : "945.60"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO));
        }
        return new Financing(installments, new BigDecimal("26.95"));
    }
}
