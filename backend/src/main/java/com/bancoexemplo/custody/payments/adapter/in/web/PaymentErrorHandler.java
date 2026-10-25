package com.bancoexemplo.custody.payments.adapter.in.web;

import com.bancoexemplo.custody.payments.domain.payment.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.payment.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.payment.InvalidPaymentException;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = PaymentController.class)
class PaymentErrorHandler {

    private static final List<String> FIELD_ORDER = Arrays.stream(RegisterPaymentRequest.class.getRecordComponents())
            .map(RecordComponent::getName)
            .toList();

    // The validator reports violations in no fixed order; the operator sees the first field of the form.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail missing(MethodArgumentNotValidException e) {
        String detail = e.getFieldErrors().stream()
                .min(Comparator.comparingInt(error -> FIELD_ORDER.indexOf(error.getField())))
                .map(FieldError::getDefaultMessage)
                .orElse("Requisição inválida");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(InvalidPaymentException.class)
    ProblemDetail invalid(InvalidPaymentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(InstallmentAlreadyPaidException.class)
    ProblemDetail alreadyPaid(InstallmentAlreadyPaidException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ContractNotFoundForPaymentException.class)
    ProblemDetail notFound(ContractNotFoundForPaymentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
