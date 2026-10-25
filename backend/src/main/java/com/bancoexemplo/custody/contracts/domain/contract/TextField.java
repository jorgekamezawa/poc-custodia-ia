package com.bancoexemplo.custody.contracts.domain.contract;

/** The rule the free-text fields of the contract share: trimmed, from 1 to 120 characters. */
final class TextField {

    private static final int MAX_LENGTH = 120;

    private TextField() {}

    static String trimmed(String value, String label) {
        String trimmed = value == null ? "" : value.strip();
        if (trimmed.isEmpty() || trimmed.length() > MAX_LENGTH) {
            throw new InvalidContractException(label + " deve ter de 1 a " + MAX_LENGTH + " caracteres");
        }
        return trimmed;
    }
}
