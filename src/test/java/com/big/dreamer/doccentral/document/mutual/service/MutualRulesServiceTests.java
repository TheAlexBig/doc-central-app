package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.mutual.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class MutualRulesServiceTests {
    private final MutualRulesService rules = new MutualRulesService(new MutualFinancialCalculator());

    @Test
    void allowsNoGuaranteeAndMovableGuaranteeForPrivateInstrument() {
        assertThat(rules.resolve(request(MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.NONE, null, "")).includeAuthentic()).isTrue();
        assertThat(rules.resolve(request(MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.MOVABLE, null, "Vehículo inscrito")).instrumentType())
                .isEqualTo(MutualInstrumentType.PRIVATE_AUTHENTICATED);
    }

    @Test
    void requiresGuarantorForPersonalGuarantee() {
        assertThatThrownBy(() -> rules.resolve(request(MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.PERSONAL_GUARANTOR, null, "")))
                .isInstanceOf(CarSaleRequestValidationException.class)
                .hasMessageContaining("combinación");
        assertThat(rules.resolve(request(MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.PERSONAL_GUARANTOR, person("Tres"), "")).guaranteeType())
                .isEqualTo(MutualGuaranteeType.PERSONAL_GUARANTOR);
    }

    @Test
    void mortgageRequiresPublicDeedAndDeedNumber() {
        assertThatThrownBy(() -> rules.resolve(request(MutualInstrumentType.PRIVATE_AUTHENTICATED,
                MutualGuaranteeType.MORTGAGE, null, "Inmueble")))
                .isInstanceOf(CarSaleRequestValidationException.class);
        MutualRulesService.Resolution publicDeed = rules.resolve(request(MutualInstrumentType.PUBLIC_DEED,
                MutualGuaranteeType.MORTGAGE, null, "Inmueble"));
        assertThat(publicDeed.includeAuthentic()).isFalse();
        assertThat(publicDeed.instrumentType()).isEqualTo(MutualInstrumentType.PUBLIC_DEED);
        assertThat(publicDeed.allowedInstrumentTypes()).containsExactly(MutualInstrumentType.PUBLIC_DEED);
        assertThat(publicDeed.requiredInstrumentType()).isEqualTo(MutualInstrumentType.PUBLIC_DEED);
    }

    private MutualDocumentRequest request(MutualInstrumentType instrument, MutualGuaranteeType guarantee,
                                          PersonDetails guarantor, String details) {
        MutualTerms terms = new MutualTerms("MIL", "DOCE MESES", "FECHA", "DOCE", "CIEN",
                "BANCO", "CUENTA", "UNO", "", "CONSUMO", false, "", "", "",
                "SANTA TECLA", "LA LIBERTAD", "FECHA", "HORA", "No", "No",
                new BigDecimal("1000"), MutualTermMode.DURATION, 12, MutualTermUnit.MONTHS,
                LocalDate.of(2026, 1, 1), null, 12, BigDecimal.ONE, instrument, guarantee,
                instrument == MutualInstrumentType.PUBLIC_DEED ? 1 : null, "UNO", details);
        return new MutualDocumentRequest(person("Uno"), person("Dos"), guarantor, terms,
                new LegalAgentDetails("Nora", "Notaria", "San Salvador", "San Salvador",
                        "Femenino", "Notario"));
    }

    private PersonDetails person(String document) {
        return new PersonDetails("Ana", document, "La Libertad", "Santa Tecla", document,
                "Femenino", "TREINTA", "Comerciante");
    }
}
