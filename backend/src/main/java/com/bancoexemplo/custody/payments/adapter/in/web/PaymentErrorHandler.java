package com.bancoexemplo.custody.payments.adapter.in.web;

import com.bancoexemplo.custody.payments.domain.ContractNotFoundForPaymentException;
import com.bancoexemplo.custody.payments.domain.InstallmentAlreadyPaidException;
import com.bancoexemplo.custody.payments.domain.InvalidPaymentException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = PaymentController.class)
class PaymentErrorHandler {

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
