package com.big.dreamer.doccentral.document.marriage.service;

import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.marriage.model.MarriageBirthCertificate;
import com.big.dreamer.doccentral.document.marriage.model.MarriageDetails;
import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.big.dreamer.doccentral.document.marriage.model.MarriageParent;
import com.big.dreamer.doccentral.document.marriage.model.MarriageParty;
import com.big.dreamer.doccentral.document.marriage.model.MarriageWitness;
import com.big.dreamer.doccentral.document.marriage.model.RecognizedChild;
import com.big.dreamer.doccentral.document.marriage.template.MarriageTemplateRepository;
import com.big.dreamer.doccentral.document.render.LegalDocumentRenderer;
import com.big.dreamer.doccentral.document.render.LegalDocumentRenderer.Section;
import com.big.dreamer.doccentral.document.render.LegalDocumentRenderer.Signature;
import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.document.text.LegalDocumentText;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MarriageDocumentService {
    private final MarriageTemplateRepository templates;
    private final MarriageRulesService rules;
    private final LegalDocumentRenderer renderer;

    public MarriageDocumentService(MarriageTemplateRepository templates, MarriageRulesService rules,
                                   LegalDocumentRenderer renderer) {
        this.templates = templates;
        this.rules = rules;
        this.renderer = renderer;
    }

    public byte[] createDocument(MarriageDocumentRequest request) {
        return renderer.word(assemble(request));
    }

    public byte[] createPdfDocument(MarriageDocumentRequest request) {
        return renderer.pdf(assemble(request));
    }

    List<Section> assemble(MarriageDocumentRequest request) {
        MarriageRuleResolution resolution = rules.validateForGeneration(request);
        Map<String, String> blocks = templates.findAll();
        Map<String, String> values = new LinkedHashMap<>(values(request, resolution));
        values.put("partyOne", party(blocks, request.partyOne(), request.details().interpreter()));
        values.put("partyTwo", party(blocks, request.partyTwo(), request.details().interpreter()));
        List<Signature> parties = List.of(
                new Signature(fullName(request.partyOne().person()), "CONTRAYENTE"),
                new Signature(fullName(request.partyTwo().person()), "CONTRAYENTE"));
        List<Signature> premaritalSignatures = new ArrayList<>(parties);
        List<Signature> ceremonySignatures = new ArrayList<>(parties);
        if (request.details().interpreter() != null) {
            Signature interpreter = new Signature(fullName(request.details().interpreter()), "INTÉRPRETE");
            premaritalSignatures.add(interpreter);
            ceremonySignatures.add(interpreter);
        }
        Signature notary = new Signature(fullName(request.legalAgent().givenName(),
                request.legalAgent().lastName()), "NOTARIO AUTORIZANTE");
        premaritalSignatures.add(notary);
        request.witnesses().forEach(witness -> ceremonySignatures.add(
                new Signature(fullName(witness.person()), "TESTIGO")));
        ceremonySignatures.add(notary);
        return List.of(
                new Section(render(blocks, "premarital.txt", values), premaritalSignatures),
                new Section(render(blocks, "marriage.txt", values), ceremonySignatures),
                new Section(render(blocks, "registration.txt", values), List.of()));
    }

    private Map<String, String> values(MarriageDocumentRequest request, MarriageRuleResolution resolution) {
        MarriageDetails details = request.details();
        String notary = fullName(request.legalAgent().givenName(), request.legalAgent().lastName());
        String regime = regime(resolution.effectivePropertyRegime());
        return Map.ofEntries(
                Map.entry("premaritalPlace", details.premaritalPlace()),
                Map.entry("premaritalState", details.premaritalState()),
                Map.entry("premaritalTime", details.premaritalTime()),
                Map.entry("premaritalDate", LegalDocumentText.date(details.premaritalDate())),
                Map.entry("celebrationPlace", details.celebrationPlace()),
                Map.entry("celebrationState", details.celebrationState()),
                Map.entry("celebrationTime", details.celebrationTime()),
                Map.entry("celebrationDate", LegalDocumentText.date(details.celebrationDate())),
                Map.entry("deedNumber", LegalDocumentText.number(details.deedNumber())),
                Map.entry("notary", notary),
                Map.entry("notaryTitle", female(request.legalAgent().gender()) ? "NOTARIA" : "NOTARIO"),
                Map.entry("propertyRegime", "Los contrayentes adoptan el régimen patrimonial de " + regime),
                Map.entry("propertyRegimeName", regime),
                Map.entry("marriedName", marriedName(request)),
                Map.entry("children", children(request.recognizedChildren())),
                Map.entry("capitulations", capitulations(details)),
                Map.entry("proxy", proxy(details)),
                Map.entry("documents", documents(request)),
                Map.entry("witnesses", witnesses(request.witnesses())),
                Map.entry("partyOneName", fullName(request.partyOne().person())),
                Map.entry("partyTwoName", fullName(request.partyTwo().person())),
                Map.entry("capitulationsRegistration", details.capitulations()
                        ? "Remitir también el instrumento de capitulaciones: " + details.capitulationsDetails() + "."
                        : "No se declararon capitulaciones previas."),
                Map.entry("childrenRegistration", request.recognizedChildren().isEmpty()
                        ? "No se reconocieron hijos en el acto."
                        : "Comunicar el reconocimiento de: " + request.recognizedChildren().stream()
                        .map(RecognizedChild::name).collect(Collectors.joining(", ")) + "."),
                Map.entry("postActions", String.join(" ", resolution.postCelebrationActions())));
    }

    private String party(Map<String, String> blocks, MarriageParty party, PersonDetails interpreter) {
        return render(blocks, "party.txt", Map.ofEntries(
                Map.entry("name", fullName(party.person())),
                Map.entry("age", party.person().age()),
                Map.entry("job", party.person().job()),
                Map.entry("nationality", party.nationality()),
                Map.entry("familyStatus", familyStatus(party)),
                Map.entry("birthPlace", party.birthPlace()),
                Map.entry("settlement", party.person().settlement()),
                Map.entry("state", party.person().state()),
                Map.entry("identityType", party.identityType()),
                Map.entry("document", party.person().document()),
                Map.entry("parents", parent(party.mother()) + " y " + parent(party.father())),
                Map.entry("languageAssistance", party.speaksSpanish() ? "" : ", asistido por el intérprete "
                        + (interpreter == null ? "PENDIENTE" : fullName(interpreter)))));
    }

    private String parent(MarriageParent parent) {
        return parent.name() + ", " + parent.job() + ", del domicilio de " + parent.settlement();
    }

    private String documents(MarriageDocumentRequest request) {
        List<String> documents = new ArrayList<>();
        documents.add(birthCertificate(request.partyOne()));
        documents.add(birthCertificate(request.partyTwo()));
        addFamilyStatusDocument(documents, request.partyOne());
        addFamilyStatusDocument(documents, request.partyTwo());
        request.recognizedChildren().forEach(child -> documents.add(
                "certificación de partida de nacimiento de " + child.name() + ", " + child.birthCertificate()));
        if (request.details().capitulations()) documents.add(request.details().capitulationsDetails());
        if (request.details().marriageByProxy()) documents.add(request.details().proxyDetails());
        return String.join("; ", documents);
    }

    private String birthCertificate(MarriageParty party) {
        MarriageBirthCertificate certificate = party.birthCertificate();
        return "certificación de partida de nacimiento de " + fullName(party.person())
                + ", número " + certificate.number()
                + optional(", folio ", certificate.folio())
                + optional(", libro ", certificate.book())
                + ", del " + certificate.registry()
                + ", expedida el " + LegalDocumentText.date(certificate.issueDate())
                + " por " + certificate.issuedBy();
    }

    private void addFamilyStatusDocument(List<String> documents, MarriageParty party) {
        if (!blank(party.familyStatusDocument())) {
            documents.add("documentación del estado familiar de " + fullName(party.person())
                    + ": " + party.familyStatusDocument());
        }
    }

    private String witnesses(List<MarriageWitness> witnesses) {
        return witnesses.stream().map(witness -> fullName(witness.person()) + ", de "
                + witness.person().age() + " años de edad, " + witness.person().job()
                + ", del domicilio de " + witness.person().settlement()
                + ", identificado por medio de documento número " + witness.person().document())
                .collect(Collectors.joining("; y "));
    }

    private String children(List<RecognizedChild> children) {
        if (children.isEmpty()) return "Los contrayentes manifiestan que no reconocerán hijos en este acto.";
        return "Los contrayentes reconocen como hijos comunes a "
                + children.stream().map(RecognizedChild::name).collect(Collectors.joining(", ")) + ".";
    }

    private String capitulations(MarriageDetails details) {
        return details.capitulations()
                ? "Existen capitulaciones matrimoniales contenidas en " + details.capitulationsDetails() + "."
                : "Los contrayentes manifiestan que no han otorgado capitulaciones matrimoniales.";
    }

    private String proxy(MarriageDetails details) {
        return details.marriageByProxy()
                ? "La comparecencia por poder se acredita con " + details.proxyDetails() + "."
                : "";
    }

    private String marriedName(MarriageDocumentRequest request) {
        PersonDetails woman = female(request.partyOne().person().gender())
                ? request.partyOne().person()
                : female(request.partyTwo().person().gender()) ? request.partyTwo().person() : null;
        if (woman == null) return "No se consigna elección de apellido posterior al matrimonio";
        if (blank(request.details().marriedName())) {
            return fullName(woman) + " manifiesta que continuará usando sus apellidos";
        }
        return fullName(woman) + " manifiesta que usará el nombre "
                + request.details().marriedName().toUpperCase(Locale.forLanguageTag("es-SV"));
    }

    private String regime(String value) {
        return switch (value) {
            case "SEPARATION_OF_PROPERTY" -> "SEPARACIÓN DE BIENES";
            case "PARTICIPATION_IN_GAINS" -> "PARTICIPACIÓN EN LAS GANANCIAS";
            default -> "COMUNIDAD DIFERIDA";
        };
    }

    private String familyStatus(MarriageParty party) {
        boolean woman = female(party.person().gender());
        return switch (party.familyStatus().toUpperCase(Locale.ROOT)) {
            case "DIVORCED" -> woman ? "DIVORCIADA" : "DIVORCIADO";
            case "WIDOWED" -> woman ? "VIUDA" : "VIUDO";
            case "MARRIED" -> woman ? "CASADA" : "CASADO";
            default -> woman ? "SOLTERA" : "SOLTERO";
        };
    }

    private String render(Map<String, String> blocks, String name, Map<String, String> values) {
        return EditableTemplateRepository.render(blocks.get(name), values);
    }

    private String optional(String prefix, String value) {
        return blank(value) ? "" : prefix + value;
    }

    private boolean female(String value) {
        return "Femenino".equalsIgnoreCase(value);
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private String fullName(PersonDetails person) {
        return fullName(person.givenName(), person.lastName());
    }

    private String fullName(String givenName, String lastName) {
        return (givenName + " " + lastName).trim().replaceAll("\\s+", " ")
                .toUpperCase(Locale.forLanguageTag("es-SV"));
    }
}
