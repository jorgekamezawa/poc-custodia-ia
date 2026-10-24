package com.bancoexemplo.custody.calculator.domain;

import com.bancoexemplo.custody.calculator.api.EffectiveCostRequest;
import com.bancoexemplo.custody.calculator.api.InvalidScheduleRequestException;

/** Release date and upfront costs the total effective cost accepts. */
public final class EffectiveCostLimits {

    private EffectiveCostLimits() {}

    public static void validate(EffectiveCostRequest request) {
        if (!request.releaseDate().isBefore(request.schedule().firstDueDate())) {
            throw new InvalidScheduleRequestException("Data de liberação deve ser anterior ao primeiro vencimento");
        }
        if (request.upfrontCosts().signum() < 0
                || request.upfrontCosts().compareTo(request.schedule().principal()) >= 0) {
            throw new InvalidScheduleRequestException(
                    "Custos antecipados devem ficar entre R$ 0,00 e menos que o valor financiado");
        }
    }
}
