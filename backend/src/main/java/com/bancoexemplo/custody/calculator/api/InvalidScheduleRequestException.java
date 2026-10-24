package com.bancoexemplo.custody.calculator.api;

/** A request outside the limits; the message names the field and is shown to the operator. */
public class InvalidScheduleRequestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidScheduleRequestException(String message) {
        super(message);
    }
}
