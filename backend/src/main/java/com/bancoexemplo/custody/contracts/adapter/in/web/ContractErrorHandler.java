package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractNotFoundException;
import com.bancoexemplo.custody.contracts.domain.contract.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.contract.InvalidContractException;
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

@RestControllerAdvice(basePackageClasses = ContractController.class)
class ContractErrorHandler {

    private static final List<String> FIELD_ORDER = Arrays.stream(RegisterContractRequest.class.getRecordComponents())
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

    @ExceptionHandler(InvalidContractException.class)
    ProblemDetail invalid(InvalidContractException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicateContractNumberException.class)
    ProblemDetail duplicate(DuplicateContractNumberException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ContractNotFoundException.class)
    ProblemDetail notFound(ContractNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
