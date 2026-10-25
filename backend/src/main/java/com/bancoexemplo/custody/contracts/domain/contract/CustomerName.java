package com.bancoexemplo.custody.contracts.domain.contract;

/** The customer's name as signed, trimmed, from 1 to 120 characters. */
public record CustomerName(String value) {

    public CustomerName {
        value = TextField.trimmed(value, "Nome do cliente");
    }
}
