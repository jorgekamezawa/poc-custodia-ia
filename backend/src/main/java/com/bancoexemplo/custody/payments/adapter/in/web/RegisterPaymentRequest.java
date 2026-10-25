package com.bancoexemplo.custody.payments.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Body of {@code POST /api/contracts/{contractId}/payments}; a missing field is refused here, before the use case. */
public record RegisterPaymentRequest(
        @NotNull(message = "Parcela é obrigatório") Integer installmentNumber,

        @NotNull(message = "Data do pagamento é obrigatório")
        LocalDate paidOn,

        @NotNull(message = "Valor é obrigatório") BigDecimal amount) {}
