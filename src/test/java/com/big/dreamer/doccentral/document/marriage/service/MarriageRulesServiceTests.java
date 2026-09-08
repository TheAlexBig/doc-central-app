package com.big.dreamer.doccentral.document.marriage.service;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.marriage.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class MarriageRulesServiceTests {
    private final MarriageRulesService rules = new MarriageRulesService();

    @Test
    void acceptsTwoAdultSinglePartiesAndUsesCommunityDeferredByDefault() {
        MarriageRuleResolution result = rules.validateForGeneration(request(
                party("Femenino", "SINGLE", LocalDate.of(1995, 1, 1)),
                party("Masculino", "SINGLE", LocalDate.of(1994, 1, 1)),
                witnesses(), details(null), List.of()));
        assertThat(result.effectivePropertyRegime()).isEqualTo("COMMUNITY_DEFERRED");
        assertThat(result.issues()).isEmpty();
    }

    @Test
    void preservesBothExplicitPropertyRegimes() {
        assertThat(rules.resolve(requestWithRegime("SEPARATION_OF_PROPERTY")).effectivePropertyRegime())
                .isEqualTo("SEPARATION_OF_PROPERTY");
        assertThat(rules.resolve(requestWithRegime("PARTICIPATION_IN_GAINS")).effectivePropertyRegime())
                .isEqualTo("PARTICIPATION_IN_GAINS");
    }

    @Test
    void requiresPreviousMarriageDocumentsForDivorcedAndWidowedParties() {
        assertThat(rules.resolve(request(
                party("Femenino", "DIVORCED", LocalDate.of(1990, 1, 1)),
                party("Masculino", "WIDOWED", LocalDate.of(1990, 1, 1)),
                witnesses(), details(null), List.of())).issues())
                .extracting(MarriageRuleResolution.Issue::code)
                .containsExactly("PREVIOUS_MARRIAGE_DOCUMENT", "PREVIOUS_MARRIAGE_DOCUMENT");
    }

    @Test
    void rejectsUnderageAndCurrentMarriage() {
        MarriageParty minor = party("Femenino", "SINGLE", LocalDate.of(2010, 1, 1));
        MarriageParty married = copy(party("Masculino", "MARRIED", LocalDate.of(1990, 1, 1)),
                true, true, false, false, false, false, true, "");
        assertThatThrownBy(() -> rules.validateForGeneration(request(
                minor, married, witnesses(), details(null), List.of())))
                .isInstanceOf(CarSaleRequestValidationException.class)
                .hasMessageContaining("requisitos pendientes");
    }

    @Test
    void rejectsOldBirthCertificate() {
        MarriageParty valid = party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1));
        MarriageBirthCertificate old = new MarriageBirthCertificate(
                "UNO", "", "", "Registro", LocalDate.of(2026, 6, 1), "Registrador");
        MarriageParty changed = new MarriageParty(valid.person(), valid.birthDate(), valid.familyStatus(),
                valid.nationality(), valid.birthPlace(), valid.identityType(), valid.mother(), valid.father(), old,
                "", false, true, false, false, false, false, true);
        assertThat(rules.resolve(request(changed, party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1)),
                witnesses(), details(null), List.of())).issues())
                .extracting(MarriageRuleResolution.Issue::code).contains("STALE_BIRTH_CERTIFICATE");
    }

    @Test
    void validatesWitnessAgeLanguageKnowledgeAndRelationship() {
        MarriageWitness invalid = new MarriageWitness(
                person("Testigo", "Menor", "Masculino", "99999999-9", "DIECISÉIS"),
                LocalDate.of(2010, 1, 1), false, false, true);
        MarriageRuleResolution result = rules.resolve(request(
                party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1)),
                party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1)),
                List.of(invalid, witnesses().get(1)), details(null), List.of()));
        assertThat(result.issues()).extracting(MarriageRuleResolution.Issue::code)
                .contains("UNDERAGE_WITNESS", "WITNESS_LANGUAGE", "WITNESS_KNOWLEDGE",
                        "WITNESS_RELATIONSHIP");
    }

    @Test
    void requiresChildCertificateAndCapitulationsInstrument() {
        MarriageDetails capitulations = new MarriageDetails(
                LocalDate.of(2026, 9, 8), "DIEZ HORAS", "San Salvador", "San Salvador",
                LocalDate.of(2026, 9, 10), "DIECISÉIS HORAS", "San Salvador", "San Salvador",
                12, null, true, "", "", false, "", null);
        MarriageRuleResolution result = rules.resolve(request(
                party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1)),
                party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1)),
                witnesses(), capitulations, List.of(new RecognizedChild("HIJO COMÚN", ""))));
        assertThat(result.issues()).extracting(MarriageRuleResolution.Issue::code)
                .contains("CHILD_BIRTH_CERTIFICATE", "CAPITULATIONS");
    }

    @Test
    void warnsForForeignPartyAndRequiresInterpreterWhenNeeded() {
        MarriageParty foreign = party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1));
        foreign = new MarriageParty(foreign.person(), foreign.birthDate(), foreign.familyStatus(),
                "ESTADOUNIDENSE", "California", "PASAPORTE", foreign.mother(), foreign.father(),
                foreign.birthCertificate(), "", false, true, false, false, false, false, false);
        MarriageRuleResolution result = rules.resolve(request(
                foreign, party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1)),
                witnesses(), details(null), List.of()));
        assertThat(result.issues()).extracting(MarriageRuleResolution.Issue::code)
                .contains("FOREIGN_PARTY", "LANGUAGE", "INTERPRETER");
    }

    @Test
    void requiresANotaryToAuthorizeMarriage() {
        MarriageDocumentRequest valid = requestWithRegime("COMMUNITY_DEFERRED");
        MarriageDocumentRequest withLawyer = new MarriageDocumentRequest(
                valid.partyOne(), valid.partyTwo(), valid.witnesses(), valid.recognizedChildren(), valid.details(),
                new LegalAgentDetails("ANA", "ABOGADA", "San Salvador", "San Salvador",
                        "Femenino", "Abogado"));

        assertThat(rules.resolve(withLawyer).issues())
                .extracting(MarriageRuleResolution.Issue::code)
                .contains("NOTARY_REQUIRED");
    }

    private MarriageDocumentRequest requestWithRegime(String regime) {
        return request(party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1)),
                party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1)),
                witnesses(), details(regime), List.of());
    }

    static MarriageDocumentRequest request(MarriageParty one, MarriageParty two,
                                           List<MarriageWitness> witnesses, MarriageDetails details,
                                           List<RecognizedChild> children) {
        return new MarriageDocumentRequest(one, two, witnesses, children, details,
                new LegalAgentDetails("ANA", "NOTARIA", "San Salvador", "San Salvador",
                        "Femenino", "Notario"));
    }

    static MarriageDetails details(String regime) {
        return new MarriageDetails(
                LocalDate.of(2026, 9, 8), "DIEZ HORAS", "San Salvador", "San Salvador",
                LocalDate.of(2026, 9, 10), "DIECISÉIS HORAS", "San Salvador", "San Salvador",
                12, regime, false, "", "", false, "", null);
    }

    static MarriageParty party(String gender, String status, LocalDate birthDate) {
        String suffix = gender.equals("Femenino") ? "1" : "2";
        return new MarriageParty(person("NOMBRE", "APELLIDO", gender, "0000000" + suffix + "-" + suffix, "TREINTA"),
                birthDate, status, "SALVADOREÑA", "San Salvador", "DUI",
                new MarriageParent("MADRE", "EMPLEADA", "San Salvador"),
                new MarriageParent("PADRE", "EMPLEADO", "San Salvador"),
                new MarriageBirthCertificate("UNO", "DOS", "TRES", "Registro del Estado Familiar",
                        LocalDate.of(2026, 8, 1), "REGISTRADOR"), "",
                false, true, false, false, false, false, true);
    }

    static List<MarriageWitness> witnesses() {
        return List.of(
                new MarriageWitness(person("TESTIGO", "UNO", "Femenino", "11111111-1", "TREINTA"),
                        LocalDate.of(1990, 1, 1), true, true, false),
                new MarriageWitness(person("TESTIGO", "DOS", "Masculino", "22222222-2", "TREINTA"),
                        LocalDate.of(1990, 1, 1), true, true, false));
    }

    static PersonDetails person(String name, String lastName, String gender, String document, String age) {
        return new PersonDetails(name, lastName, "San Salvador", "San Salvador",
                document, gender, age, "EMPLEADO");
    }

    private MarriageParty copy(MarriageParty value, boolean currentMarriage, boolean canConsent,
                               boolean kinship, boolean adoption, boolean homicide, boolean tutor,
                               boolean speaksSpanish, String statusDocument) {
        return new MarriageParty(value.person(), value.birthDate(), value.familyStatus(), value.nationality(),
                value.birthPlace(), value.identityType(), value.mother(), value.father(), value.birthCertificate(),
                statusDocument, currentMarriage, canConsent, kinship, adoption, homicide, tutor, speaksSpanish);
    }
}
