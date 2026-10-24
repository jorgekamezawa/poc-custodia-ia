package com.bancoexemplo.custody.contracts.domain;

/** Required free-text fields of the contract, trimmed and limited in length. */
public final class TextField {

    private static final int MAX_LENGTH = 120;

    private TextField() {}

    public static String customerName(String value) {
        return required(value, "Nome do cliente");
    }

    public static String vehicleDescription(String value) {
        return required(value, "Descrição do veículo");
    }

    private static String required(String value, String label) {
        String trimmed = value == null ? "" : value.strip();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            throw new InvalidContractException(label + " deve ter de 1 a " + MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }
}
