package com.bancoexemplo.custody.contracts.domain.contract;

import com.bancoexemplo.custody.contracts.domain.contract.exception.InvalidContractException;
import java.util.regex.Pattern;

/** The number the contract was signed with, unique in the custody. */
public record ContractNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("[A-Za-z0-9-]{1,30}");

    public ContractNumber {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new InvalidContractException("Número do contrato deve ter de 1 a 30 letras, números ou hífen");
        }
    }
}
