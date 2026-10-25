package com.bancoexemplo.custody.payments.adapter.in.web;

import com.bancoexemplo.custody.payments.application.PaymentQuote;
import com.bancoexemplo.custody.payments.application.RegisterPaymentCommand;
import com.bancoexemplo.custody.payments.application.RegisterPaymentService;
import com.bancoexemplo.custody.payments.domain.payment.Payment;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PaymentController {

    private final RegisterPaymentService registerPayment;

    PaymentController(RegisterPaymentService registerPayment) {
        this.registerPayment = registerPayment;
    }

    @PostMapping("/api/contracts/{contractId}/payments")
    ResponseEntity<Payment> register(
            @PathVariable UUID contractId, @RequestBody @Valid RegisterPaymentRequest request, Authentication user) {
        Payment payment = registerPayment.register(new RegisterPaymentCommand(
                contractId, request.installmentNumber(), request.paidOn(), request.amount(), user.getName()));
        return ResponseEntity.created(URI.create("/api/contracts/" + contractId + "/payments/" + payment.getId()))
                .body(payment);
    }

    @GetMapping("/api/contracts/{contractId}/payments/quote")
    PaymentQuote quote(
            @PathVariable UUID contractId,
            @RequestParam int installmentNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate paidOn) {
        return registerPayment.quote(contractId, installmentNumber, paidOn);
    }
}
