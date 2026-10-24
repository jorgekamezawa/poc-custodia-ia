package com.bancoexemplo.custody.contracts.adapter.in.web;

import com.bancoexemplo.custody.contracts.application.ContractNotFoundException;
import com.bancoexemplo.custody.contracts.domain.DuplicateContractNumberException;
import com.bancoexemplo.custody.contracts.domain.InvalidContractException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackageClasses = ContractController.class)
class ContractErrorHandler {

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
