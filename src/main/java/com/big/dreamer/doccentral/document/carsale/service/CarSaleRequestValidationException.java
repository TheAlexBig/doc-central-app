package com.big.dreamer.doccentral.document.carsale.service;

import java.util.Map;

public class CarSaleRequestValidationException extends RuntimeException {

    private final Map<String, String> fields;

    public CarSaleRequestValidationException(String message, Map<String, String> fields) {
        super(message);
        this.fields = Map.copyOf(fields);
    }

    public Map<String, String> fields() {
        return fields;
    }
}
