package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;

import java.util.Locale;
import java.util.Map;

public final class MutualRequestValidator {

    private MutualRequestValidator() {
    }

    public static void validate(MutualDocumentRequest request) {
        if (!"notario".equals(normalize(request.legalAgent().role()))) {
            throw new CarSaleRequestValidationException(
                    "Seleccione un notario para autenticar el mutuo.",
                    Map.of("agente_juridico.rol", "La auténtica debe ser autorizada por un notario."));
        }
        if (identifier(request.debtor().document()).equals(identifier(request.creditor().document()))) {
            throw new CarSaleRequestValidationException(
                    "El deudor y el acreedor deben ser personas diferentes.",
                    Map.of(
                            "deudor.documento", "Debe ser diferente al DUI del acreedor.",
                            "acreedor.documento", "Debe ser diferente al DUI del deudor."));
        }
        if (request.terms().billOfExchangeGuarantee()
                && blank(request.terms().guaranteeDueDate())) {
            throw new CarSaleRequestValidationException(
                    "Indique el vencimiento de la letra de cambio.",
                    Map.of("condiciones.fecha_vencimiento_garantia", "Campo requerido para esta garantía."));
        }
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static String identifier(String value) {
        return value == null ? "" : value.replaceAll("[^\\p{L}\\p{N}]", "").toUpperCase(Locale.ROOT);
    }
}
