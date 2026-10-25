package com.bancoexemplo.custody.contracts.domain.contract;

import java.util.regex.Pattern;

/** Customer's CPF, digits only, with valid check digits. */
public record Cpf(String digits) {

    private static final Pattern ELEVEN_DIGITS = Pattern.compile("\\d{11}");
    private static final Pattern SAME_DIGIT = Pattern.compile("(\\d)\\1{10}");

    public Cpf {
        if (digits == null
                || !ELEVEN_DIGITS.matcher(digits).matches()
                || SAME_DIGIT.matcher(digits).matches()
                || checkDigit(digits, 9) != digits.charAt(9) - '0'
                || checkDigit(digits, 10) != digits.charAt(10) - '0') {
            throw new InvalidContractException("CPF inválido");
        }
    }

    private static int checkDigit(String digits, int length) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += (digits.charAt(i) - '0') * (length + 1 - i);
        }
        return sum * 10 % 11 % 10;
    }
}
