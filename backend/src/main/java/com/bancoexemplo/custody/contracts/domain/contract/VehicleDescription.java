package com.bancoexemplo.custody.contracts.domain.contract;

/** The financed vehicle as described in the contract, trimmed, from 1 to 120 characters. */
public record VehicleDescription(String value) {

    public VehicleDescription {
        value = TextField.trimmed(value, "Descrição do veículo");
    }
}
