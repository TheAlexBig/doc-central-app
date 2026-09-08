package com.big.dreamer.doccentral.document.marriage.service;

import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.big.dreamer.doccentral.document.marriage.model.MarriageParty;
import com.big.dreamer.doccentral.document.marriage.model.MarriageWitness;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.big.dreamer.doccentral.document.marriage.service.MarriageRuleResolution.Severity.*;

@Service
public class MarriageRulesService {

    public MarriageRuleResolution resolve(MarriageDocumentRequest request) {
        List<MarriageRuleResolution.Issue> issues = new ArrayList<>();
        if (!"NOTARIO".equals(normalize(request.legalAgent().role()))) {
            issue(issues, ERROR, "agente_juridico.rol", "NOTARY_REQUIRED",
                    "El matrimonio debe ser autorizado por un notario.");
        }
        String regime = effectiveRegime(request.details().propertyRegime());
        validateParty(request.partyOne(), "contrayente_uno", request.details().premaritalDate(), issues);
        validateParty(request.partyTwo(), "contrayente_dos", request.details().premaritalDate(), issues);
        validateRelationship(request, issues);
        validateWitnesses(request, issues);
        validateDocuments(request, issues);
        validateSpecialCases(request, issues);
        if (request.details().celebrationDate().isBefore(request.details().premaritalDate())) {
            issue(issues, ERROR, "datos.fecha_celebracion", "CELEBRATION_BEFORE_FILE",
                    "La celebración no puede ser anterior al acta prematrimonial.");
        }
        return new MarriageRuleResolution(regime, List.copyOf(issues), List.of(
                "Remitir el testimonio de la escritura al Registro del Estado Familiar competente dentro de quince días hábiles.",
                "Verificar la inscripción del matrimonio y las marginaciones de las partidas de nacimiento.",
                "Remitir las capitulaciones y datos del régimen patrimonial cuando correspondan.",
                "Incluir el reconocimiento de hijos en la comunicación registral cuando corresponda."));
    }

    public MarriageRuleResolution validateForGeneration(MarriageDocumentRequest request) {
        MarriageRuleResolution resolution = resolve(request);
        Map<String, String> fields = new LinkedHashMap<>();
        resolution.issues().stream()
                .filter(issue -> issue.severity() != WARNING)
                .forEach(issue -> fields.putIfAbsent(issue.field(), issue.message()));
        if (!fields.isEmpty()) {
            throw new CarSaleRequestValidationException(
                    "El expediente matrimonial tiene requisitos pendientes.", fields);
        }
        return resolution;
    }

    private void validateParty(MarriageParty party, String field, LocalDate actDate,
                               List<MarriageRuleResolution.Issue> issues) {
        if (Period.between(party.birthDate(), actDate).getYears() < 18) {
            issue(issues, ERROR, field + ".fecha_nacimiento", "UNDERAGE",
                    "El contrayente debe tener al menos dieciocho años.");
        }
        if (party.currentMarriage() || "MARRIED".equals(normalize(party.familyStatus()))) {
            issue(issues, ERROR, field + ".estado_familiar", "CURRENT_MARRIAGE",
                    "No puede existir un vínculo matrimonial vigente.");
        }
        if (!party.canConsent()) issue(issues, ERROR, field + ".puede_consentir", "CONSENT",
                "El contrayente debe poder expresar consentimiento inequívoco.");
        if (party.prohibitedKinship()) issue(issues, ERROR, field + ".parentesco_impediente", "KINSHIP",
                "Existe parentesco que impide el matrimonio.");
        if (party.prohibitedAdoptionRelationship()) issue(issues, ERROR,
                field + ".relacion_adopcion_impediente", "ADOPTION",
                "Existe una relación de adopción que impide el matrimonio.");
        if (party.spouseHomicideRestriction()) issue(issues, ERROR,
                field + ".restriccion_homicidio", "HOMICIDE",
                "Existe impedimento relacionado con homicidio doloso del cónyuge anterior.");
        if (party.tutorRestriction()) issue(issues, WARNING, field + ".restriccion_tutela", "TUTOR",
                "Revise judicialmente las cuentas y obligaciones del tutor antes de celebrar.");
        LocalDate issued = party.birthCertificate().issueDate();
        if (issued.isAfter(actDate) || issued.isBefore(actDate.minusMonths(2))) {
            issue(issues, REQUIRED, field + ".partida_nacimiento.fecha_expedicion", "STALE_BIRTH_CERTIFICATE",
                    "La partida de nacimiento debe haberse expedido dentro de los dos meses anteriores al acta.");
        }
        String status = normalize(party.familyStatus());
        if (Set.of("DIVORCED", "WIDOWED").contains(status) && blank(party.familyStatusDocument())) {
            issue(issues, REQUIRED, field + ".documento_estado_familiar", "PREVIOUS_MARRIAGE_DOCUMENT",
                    status.equals("DIVORCED")
                            ? "Adjunte certificación de divorcio o sentencia ejecutoriada de nulidad."
                            : "Adjunte certificación de defunción del cónyuge anterior.");
        }
        if (!party.speaksSpanish()) {
            issue(issues, WARNING, field + ".se_expresa_castellano", "LANGUAGE",
                    "El contrayente requiere intérprete y revisión de las diligencias de traducción.");
        }
        if (!normalize(party.nationality()).contains("SALVADORE")) {
            issue(issues, WARNING, field + ".nacionalidad", "FOREIGN_PARTY",
                    "Revise vigencia, apostilla o legalización y traducción de documentos extranjeros.");
        }
    }

    private void validateRelationship(MarriageDocumentRequest request,
                                      List<MarriageRuleResolution.Issue> issues) {
        if (identifier(request.partyOne().person().document())
                .equals(identifier(request.partyTwo().person().document()))) {
            issue(issues, ERROR, "contrayente_dos.persona.documento", "DUPLICATE_PARTY",
                    "Los contrayentes deben ser personas diferentes.");
        }
    }

    private void validateWitnesses(MarriageDocumentRequest request,
                                   List<MarriageRuleResolution.Issue> issues) {
        if (request.witnesses().size() < 2) {
            issue(issues, REQUIRED, "testigos", "WITNESS_COUNT", "Se requieren por lo menos dos testigos.");
        }
        Set<String> identifiers = new java.util.HashSet<>();
        for (int index = 0; index < request.witnesses().size(); index++) {
            MarriageWitness witness = request.witnesses().get(index);
            String field = "testigos[" + index + "]";
            if (Period.between(witness.birthDate(), request.details().celebrationDate()).getYears() < 18) {
                issue(issues, ERROR, field + ".fecha_nacimiento", "UNDERAGE_WITNESS",
                        "Cada testigo debe tener al menos dieciocho años.");
            }
            if (!witness.readsWritesSpanish()) issue(issues, ERROR, field + ".lee_escribe_castellano",
                    "WITNESS_LANGUAGE", "El testigo debe saber leer y escribir castellano.");
            if (!witness.knowsParties()) issue(issues, ERROR, field + ".conoce_contrayentes",
                    "WITNESS_KNOWLEDGE", "El testigo debe conocer a los contrayentes.");
            if (witness.prohibitedRelationship()) issue(issues, ERROR, field + ".relacion_prohibida",
                    "WITNESS_RELATIONSHIP", "El testigo está comprendido en una prohibición legal.");
            String id = identifier(witness.person().document());
            if (!identifiers.add(id) || id.equals(identifier(request.partyOne().person().document()))
                    || id.equals(identifier(request.partyTwo().person().document()))) {
                issue(issues, ERROR, field + ".persona.documento", "DUPLICATE_WITNESS",
                        "Cada testigo debe ser una persona distinta de los contrayentes y demás testigos.");
            }
        }
    }

    private void validateDocuments(MarriageDocumentRequest request,
                                   List<MarriageRuleResolution.Issue> issues) {
        for (int index = 0; index < request.recognizedChildren().size(); index++) {
            if (blank(request.recognizedChildren().get(index).birthCertificate())) {
                issue(issues, REQUIRED, "hijos_reconocidos[" + index + "].partida_nacimiento",
                        "CHILD_BIRTH_CERTIFICATE", "Adjunte la partida de nacimiento del hijo que será reconocido.");
            }
        }
        if (request.details().capitulations() && blank(request.details().capitulationsDetails())) {
            issue(issues, REQUIRED, "datos.detalle_capitulaciones", "CAPITULATIONS",
                    "Indique el instrumento que contiene las capitulaciones matrimoniales.");
        }
    }

    private void validateSpecialCases(MarriageDocumentRequest request,
                                      List<MarriageRuleResolution.Issue> issues) {
        if (request.details().marriageByProxy()) {
            if (blank(request.details().proxyDetails())) {
                issue(issues, REQUIRED, "datos.detalle_poder", "PROXY_DOCUMENT",
                        "Indique el poder especial para contraer matrimonio.");
            }
            issue(issues, WARNING, "datos.matrimonio_por_poder", "PROXY_REVIEW",
                    "Matrimonio por poder requiere revisión notarial del mandato y comparecencia.");
        }
        boolean interpreterRequired = !request.partyOne().speaksSpanish() || !request.partyTwo().speaksSpanish();
        if (interpreterRequired && request.details().interpreter() == null) {
            issue(issues, REQUIRED, "datos.interprete", "INTERPRETER",
                    "Seleccione intérprete para el contrayente que no se expresa en castellano.");
        }
    }

    private String effectiveRegime(String value) {
        String normalized = normalize(value);
        return Set.of("SEPARATION_OF_PROPERTY", "PARTICIPATION_IN_GAINS", "COMMUNITY_DEFERRED")
                .contains(normalized) ? normalized : "COMMUNITY_DEFERRED";
    }

    private void issue(List<MarriageRuleResolution.Issue> issues,
                       MarriageRuleResolution.Severity severity, String field, String code, String message) {
        issues.add(new MarriageRuleResolution.Issue(severity, field, code, message));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String identifier(String value) {
        return normalize(value).replaceAll("[^\\p{L}\\p{N}]", "");
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
