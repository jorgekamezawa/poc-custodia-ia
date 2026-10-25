package com.bancoexemplo.custody.contracts.domain.contract;

import java.util.regex.Pattern;

/** Vehicle identification number: 17 letters and digits, without I, O and Q. */
public record ChassisNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-HJ-NPR-Z0-9]{17}");

    public ChassisNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidContractException("Chassi deve ter 17 letras e números, sem I, O e Q");
        }
    }
}
