package com.bancoexemplo.custody.payments.adapter.in.web;

import com.bancoexemplo.custody.payments.application.RegisterPaymentCommand;
import com.bancoexemplo.custody.payments.application.RegisterPaymentService;
import com.bancoexemplo.custody.payments.domain.InvalidPaymentException;
import com.bancoexemplo.custody.payments.domain.Payment;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class PaymentController {

    private final RegisterPaymentService registerPayment;

    PaymentController(RegisterPaymentService registerPayment) {
        this.registerPayment = registerPayment;
    }

    @PostMapping("/api/contracts/{contractId}/payments")
    ResponseEntity<Payment> register(
            @PathVariable UUID contractId, @RequestBody RegisterPaymentRequest request, Authentication user) {
        Payment payment = registerPayment.register(new RegisterPaymentCommand(
                contractId,
                required(request.installmentNumber(), "Parcela"),
                required(request.paidOn(), "Data do pagamento"),
                required(request.amount(), "Valor"),
                user.getName()));
        return ResponseEntity.created(URI.create("/api/contracts/" + contractId + "/payments/" + payment.id()))
                .body(payment);
    }

    private static <T> T required(T value, String label) {
        if (value == null) {
            throw new InvalidPaymentException(label + " é obrigatório");
        }
        return value;
    }
}
