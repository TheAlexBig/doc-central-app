package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualGuaranteeType;
import com.big.dreamer.doccentral.document.mutual.model.MutualInstrumentType;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class MutualRulesService {
    private final MutualFinancialCalculator calculator;

    public MutualRulesService(MutualFinancialCalculator calculator) {
        this.calculator = calculator;
    }

    public Resolution resolve(MutualDocumentRequest request) {
        MutualInstrumentType instrument = request.terms().effectiveInstrumentType();
        MutualGuaranteeType guarantee = request.terms().effectiveGuaranteeType();
        if (guarantee == MutualGuaranteeType.MORTGAGE && instrument != MutualInstrumentType.PUBLIC_DEED) {
            invalid("condiciones.tipo_instrumento", "La garantía hipotecaria requiere escritura pública.");
        }
        if (instrument == MutualInstrumentType.PUBLIC_DEED
                && (request.terms().deedNumber() == null || request.terms().deedNumber() <= 0)) {
            invalid("condiciones.numero_escritura", "Indique un número de escritura válido.");
        }
        if (guarantee == MutualGuaranteeType.PERSONAL_GUARANTOR && request.guarantor() == null) {
            invalid("fiador", "Indique los datos del fiador o garante.");
        }
        if (guarantee == MutualGuaranteeType.VEHICLE_PLEDGE && request.vehiclePledge() == null) {
            invalid("garantia_prendaria", "Indique el vehículo dado en garantía y su valor.");
        }
        if (request.terms().guaranteeType() != null
                && (guarantee == MutualGuaranteeType.MOVABLE || guarantee == MutualGuaranteeType.MORTGAGE)
                && blank(request.terms().guaranteeDetails())) {
            invalid("condiciones.detalles_garantia", "Describa el bien dado en garantía.");
        }
        MutualFinancialPlan plan = request.terms().hasStructuredCalculation()
                ? calculator.calculate(request.terms()) : null;
        Set<MutualInstrumentType> allowed = guarantee == MutualGuaranteeType.MORTGAGE
                ? Set.of(MutualInstrumentType.PUBLIC_DEED)
                : Set.of(MutualInstrumentType.PRIVATE_AUTHENTICATED, MutualInstrumentType.PUBLIC_DEED);
        MutualInstrumentType required = guarantee == MutualGuaranteeType.MORTGAGE
                ? MutualInstrumentType.PUBLIC_DEED : null;
        return new Resolution(instrument, guarantee, allowed, required,
                instrument == MutualInstrumentType.PRIVATE_AUTHENTICATED, plan);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private void invalid(String field, String message) {
        throw new CarSaleRequestValidationException("La combinación seleccionada no es válida.", Map.of(field, message));
    }

    public record Resolution(MutualInstrumentType instrumentType, MutualGuaranteeType guaranteeType,
                             Set<MutualInstrumentType> allowedInstrumentTypes,
                             MutualInstrumentType requiredInstrumentType,
                             boolean includeAuthentic, MutualFinancialPlan financialPlan) {
    }
}
