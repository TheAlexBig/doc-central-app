package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;

import java.util.Locale;
import java.util.Map;

public final class CarSaleRequestValidator {

    private CarSaleRequestValidator() {
    }

    public static void validate(CarSaleDocumentRequest request) {
        if (!"notario".equals(normalizeText(request.legalAgent().role()))) {
            throw new CarSaleRequestValidationException(
                    "Seleccione un notario para autenticar la compraventa.",
                    Map.of("agente_juridico.rol", "La auténtica debe ser autorizada por un notario."));
        }
        if (normalizeIdentifier(request.seller().document())
                .equals(normalizeIdentifier(request.buyer().document()))) {
            throw new CarSaleRequestValidationException(
                    "El comprador y el vendedor deben ser personas diferentes.",
                    Map.of(
                            "comprador.documento", "Debe ser diferente al DUI del vendedor.",
                            "vendedor.documento", "Debe ser diferente al DUI del comprador."));
        }
    }

    private static String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String normalizeIdentifier(String value) {
        return value == null
                ? ""
                : value.replaceAll("[^\\p{L}\\p{N}]", "").toUpperCase(Locale.ROOT);
    }
}
