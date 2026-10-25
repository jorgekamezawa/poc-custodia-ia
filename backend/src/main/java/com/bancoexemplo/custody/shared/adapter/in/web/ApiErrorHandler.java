package com.bancoexemplo.custody.shared.adapter.in.web;

import com.bancoexemplo.custody.shared.domain.exception.BusinessRuleException;
import com.bancoexemplo.custody.shared.domain.exception.ConflictException;
import com.bancoexemplo.custody.shared.domain.exception.NotFoundException;
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

/** Turns each category of domain refusal, and a request missing a field, into its response for every module. */
@RestControllerAdvice
class ApiErrorHandler {

    @ExceptionHandler(BusinessRuleException.class)
    ProblemDetail businessRule(BusinessRuleException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    ProblemDetail conflict(ConflictException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // The validator reports violations in no fixed order; the operator sees the first field of the form.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalidRequest(MethodArgumentNotValidException e) {
        List<String> fieldOrder = fieldOrder(e.getParameter().getParameterType());
        String detail = e.getFieldErrors().stream()
                .min(Comparator.comparingInt(error -> fieldOrder.indexOf(error.getField())))
                .map(FieldError::getDefaultMessage)
                .orElse("Requisição inválida");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    private static List<String> fieldOrder(Class<?> request) {
        RecordComponent[] components = request.getRecordComponents();
        return components == null
                ? List.of()
                : Arrays.stream(components).map(RecordComponent::getName).toList();
    }
}
