package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualTerms;
import com.big.dreamer.doccentral.document.mutual.template.MutualTemplateRepository;
import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import com.big.dreamer.doccentral.document.render.LegalDocument;
import com.big.dreamer.doccentral.document.render.LegalDocument.Section;
import com.big.dreamer.doccentral.document.render.LegalDocument.Signature;
import com.big.dreamer.doccentral.document.render.LegalDocumentRenderer;
import com.big.dreamer.doccentral.document.text.LegalDocumentText;
import com.big.dreamer.doccentral.document.text.NotarialIdentificationText;
import java.util.Map;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class MutualDocumentService {

    private final MutualTemplateRepository repository;
    private final MutualRulesService rules;
    private final LegalDocumentRenderer renderer;

    public MutualDocumentService(MutualTemplateRepository repository, MutualRulesService rules,
                                 LegalDocumentRenderer renderer) {
        this.repository = repository;
        this.rules = rules;
        this.renderer = renderer;
    }

    private String render(Map<String, String> templates, String name, Map<String, String> values) {
        return EditableTemplateRepository.render(templates.get(name), values);
    }

    public byte[] createDocument(MutualDocumentRequest request) {
        return renderer.word(document(assemble(request)));
    }

    public byte[] createPdfDocument(MutualDocumentRequest request) {
        return renderer.pdf(document(assemble(request)));
    }

    private LegalDocument document(MutualDocumentContent content) {
        List<Signature> signatures = new ArrayList<>();
        signatures.add(new Signature(content.debtorName(), content.debtorTitle()));
        signatures.add(new Signature(content.creditorName(), content.creditorTitle()));
        if (!blank(content.guarantorName())) {
            signatures.add(new Signature(content.guarantorName(), content.guarantorTitle()));
        }
        List<Section> sections = new ArrayList<>();
        sections.add(new Section(content.contract(), signatures));
        if (!blank(content.authentic())) sections.add(new Section(content.authentic(), signatures));
        return new LegalDocument(sections);
    }

    MutualDocumentContent assemble(MutualDocumentRequest request) {
        Map<String, String> templates = repository.findAll();
        PersonDetails debtor = request.debtor();
        PersonDetails creditor = request.creditor();
        MutualTerms terms = request.terms();
        MutualRulesService.Resolution resolution = rules.resolve(request);
        String debtorRole = female(debtor) ? "LA DEUDORA" : "EL DEUDOR";
        String creditorRole = female(creditor) ? "LA ACREEDORA" : "EL ACREEDOR";
        String notary = fullName(request.legalAgent().givenName(), request.legalAgent().lastName());
        String notaryTitle = "Femenino".equalsIgnoreCase(request.legalAgent().gender()) ? "NOTARIA" : "NOTARIO";
        String clauses = clauses(templates, request, resolution);
        String contract = resolution.includeAuthentic()
                ? render(templates, "contract.txt", Map.ofEntries(
                    Map.entry("debtor", person(templates, debtor)),
                    Map.entry("debtorRole", debtorRole),
                    Map.entry("creditor", person(templates, creditor)),
                    Map.entry("creditorRole", creditorRole),
                    Map.entry("mutualType", mutualType(resolution.guaranteeType())),
                    Map.entry("clauses", clauses),
                    Map.entry("signingPlace", terms.signingPlace()),
                    Map.entry("signingState", terms.signingState()),
                    Map.entry("signingDate", terms.signingDate())))
                : render(templates, "public-deed.txt", Map.ofEntries(
                    Map.entry("deedNumber", value(terms.deedNumberText(), String.valueOf(terms.deedNumber()))),
                    Map.entry("signingPlace", terms.signingPlace()),
                    Map.entry("signingState", terms.signingState()),
                    Map.entry("signingTime", terms.signingTime()),
                    Map.entry("signingDate", terms.signingDate()),
                    Map.entry("notary", notary),
                    Map.entry("notaryTitle", notaryTitle),
                    Map.entry("debtor", identified(templates, debtor, terms.identifiesDebtor())),
                    Map.entry("creditor", identified(templates, creditor, terms.identifiesCreditor())),
                    Map.entry("mutualType", mutualType(resolution.guaranteeType())),
                    Map.entry("clauses", clauses)));
        String authentic = !resolution.includeAuthentic() ? "" : render(templates, "authentic.txt", Map.ofEntries(
                Map.entry("signingPlace", terms.signingPlace()),
                Map.entry("signingState", terms.signingState()),
                Map.entry("signingTime", terms.signingTime()),
                Map.entry("signingDate", terms.signingDate()),
                Map.entry("notary", notary),
                Map.entry("notaryTitle", notaryTitle),
                Map.entry("notaryPlace", request.legalAgent().settlement()),
                Map.entry("notaryState", request.legalAgent().state()),
                Map.entry("identifiedDebtor", identified(templates, debtor, terms.identifiesDebtor())),
                Map.entry("debtorRole", debtorRole),
                Map.entry("identifiedCreditor", identified(templates, creditor, terms.identifiesCreditor())),
                Map.entry("creditorRole", creditorRole),
                Map.entry("contract", contract)));
        PersonDetails guarantor = request.guarantor();
        String guarantorRole = guarantor == null ? "" : female(guarantor) ? "LA FIADORA" : "EL FIADOR";
        return new MutualDocumentContent(contract, authentic, fullName(debtor), fullName(creditor),
                debtorRole, creditorRole, guarantor == null ? "" : fullName(guarantor), guarantorRole);
    }

    private String clauses(Map<String, String> templates, MutualDocumentRequest request,
                           MutualRulesService.Resolution resolution) {
        MutualTerms terms = request.terms();
        PersonDetails debtor = request.debtor();
        PersonDetails creditor = request.creditor();
        String debtorText = female(debtor) ? "la deudora" : "el deudor";
        String fromCreditor = female(creditor) ? "de la acreedora" : "del acreedor";
        String forCreditor = female(creditor) ? "a favor de la acreedora" : "a favor del acreedor";
        String installments = "UNO".equalsIgnoreCase(terms.installmentCount())
                || "UNA".equalsIgnoreCase(terms.installmentCount())
                ? "UNA CUOTA"
                : terms.installmentCount() + " CUOTAS";
        List<String> clauses = new ArrayList<>();
        MutualFinancialPlan plan = resolution.financialPlan();
        clauses.add(render(templates, "principal.txt", Map.ofEntries(
                Map.entry("debtorSubject", capitalize(debtorText)),
                Map.entry("fromCreditor", fromCreditor),
                Map.entry("amount", plan == null ? currency(terms.amount()) : money(plan.capital())))));
        clauses.add(render(templates, "payment.txt", Map.ofEntries(
                Map.entry("term", terms.term()),
                Map.entry("dueDate", plan == null ? terms.dueDate() : formatDate(plan.dueDate())),
                Map.entry("installments", installments),
                Map.entry("installmentAmount", plan == null
                        ? currency(terms.installmentAmount()) : installmentDescription(plan)),
                Map.entry("paymentAccount", terms.paymentAccount()),
                Map.entry("paymentBank", terms.paymentBank()),
                Map.entry("creditorName", fullName(creditor)))));
        if (!blank(terms.monthlyInterest()) || !blank(terms.defaultInterest())) {
            clauses.add(render(templates, "interest.txt", Map.ofEntries(
                    Map.entry("interestTerms", interestTerms(terms)),
                    Map.entry("monthlyInterest", value(terms.monthlyInterest(), "cero")),
                    Map.entry("defaultInterest", value(terms.defaultInterest(), "cero")))));
        }
        if (resolution.financialPlan() != null) {
            clauses.add(render(templates, "payment-schedule.txt", Map.ofEntries(
                    Map.entry("number", number(clauses.size() + 1)),
                    Map.entry("capital", money(plan.capital())),
                    Map.entry("interest", money(plan.interest())),
                    Map.entry("total", money(plan.total())),
                    Map.entry("periodicity", legalPeriodicity(plan)),
                    Map.entry("schedule", scheduleSummary(plan)))));
        }
        clauses.add(render(templates, "purpose.txt", Map.ofEntries(
                Map.entry("number", number(clauses.size() + 1)),
                Map.entry("debtorSubject", capitalize(debtorText)),
                Map.entry("fundsPurpose", terms.fundsPurpose()))));
        if (terms.billOfExchangeGuarantee() && terms.guaranteeType() == null) {
            clauses.add(render(templates, "guarantee.txt", Map.ofEntries(
                    Map.entry("number", number(clauses.size() + 1)),
                    Map.entry("debtorSubject", capitalize(debtorText)),
                    Map.entry("amount", currency(terms.amount())),
                    Map.entry("forCreditor", forCreditor),
                    Map.entry("guaranteeDueDate", terms.guaranteeDueDate()))));
        } else if (resolution.guaranteeType()
                != com.big.dreamer.doccentral.document.mutual.model.MutualGuaranteeType.NONE) {
            String template = switch (resolution.guaranteeType()) {
                case PERSONAL_GUARANTOR -> "personal-guarantee.txt";
                case VEHICLE_PLEDGE -> "vehicle-pledge.txt";
                case MOVABLE -> "movable-guarantee.txt";
                case MORTGAGE -> "mortgage-guarantee.txt";
                default -> throw new IllegalStateException("Tipo de garantía no soportado.");
            };
            Map<String, String> values = new java.util.LinkedHashMap<>();
            values.put("number", number(clauses.size() + 1));
            values.put("guaranteeDetails", value(terms.guaranteeDetails(), ""));
            if (request.guarantor() != null) {
                values.put("guarantor", person(templates, request.guarantor()));
            }
            if (request.vehiclePledge() != null) {
                CarDetails vehicle = request.vehiclePledge().vehicle();
                values.put("valuation", request.vehiclePledge().valuation());
                values.put("licensePlate", vehicle.licensePlate());
                values.put("factoryYear", vehicle.factoryYear());
                values.put("brand", vehicle.brand());
                values.put("model", vehicle.model());
                values.put("capacity", vehicle.capacity());
                values.put("vehicleType", vehicle.vehicleType());
                values.put("vehicleClass", vehicle.vehicleClass());
                values.put("domain", vehicle.domain());
                values.put("color", vehicle.color());
                values.put("chassisNumber", vehicle.chassisNumber());
                values.put("engineNumber", vehicle.engineNumber());
                values.put("vinNumber", vehicle.vinNumber());
            }
            clauses.add(render(templates, template, values));
        }
        clauses.add(render(templates, "acceleration-causes.txt", Map.ofEntries(
                Map.entry("number", number(clauses.size() + 1)),
                Map.entry("debtorText", debtorText))));
        clauses.add(render(templates, "acceleration.txt", Map.ofEntries(
                Map.entry("number", number(clauses.size() + 1)))));
        String expenses = blank(terms.administrativeExpenses()) ? "" : render(templates, "expenses.txt", Map.ofEntries(
                Map.entry("debtorSubject", capitalize(debtorText)),
                Map.entry("administrativeExpenses", terms.administrativeExpenses())));
        clauses.add(render(templates, "jurisdiction.txt", Map.ofEntries(
                Map.entry("number", number(clauses.size() + 1)),
                Map.entry("debtorText", debtorText),
                Map.entry("specialDomicile", terms.signingPlace()),
                Map.entry("expenses", expenses))));
        return String.join(" ", clauses);
    }

    private String scheduleSummary(MutualFinancialPlan plan) {
        String firstDueDate = formatDate(plan.installments().getFirst().dueDate());
        if (plan.installmentCount() == 1) {
            return "La única cuota vencerá el " + firstDueDate;
        }
        String lastDueDate = formatDate(plan.installments().getLast().dueDate());
        return "La primera cuota vencerá el " + firstDueDate
                + "; las cuotas sucesivas vencerán conforme a esa periodicidad hasta la última, que vencerá el "
                + lastDueDate;
    }

    private String legalPeriodicity(MutualFinancialPlan plan) {
        if (plan.installmentCount() == 1) return "en un único pago";
        String periodicity = LegalDocumentText.replaceDigits(plan.periodicity());
        if (periodicity.startsWith("Cada ")) {
            return periodicity.replaceFirst("^Cada UNO ", "cada UN ")
                    .replaceFirst("^Cada ", "cada ");
        }
        return "en " + Character.toLowerCase(periodicity.charAt(0)) + periodicity.substring(1);
    }

    private String installmentDescription(MutualFinancialPlan plan) {
        BigDecimal first = plan.installments().getFirst().total();
        BigDecimal last = plan.installments().getLast().total();
        return first.compareTo(last) == 0
                ? money(first)
                : money(first) + " cada una, salvo la última de " + money(last)
                + " por ajuste de centavos";
    }

    private String interestTerms(MutualTerms terms) {
        List<String> provisions = new ArrayList<>();
        if (!blank(terms.monthlyInterest())) {
            provisions.add("Las cuotas pactadas ya comprenden el interés ordinario calculado a una tasa de "
                    + terms.monthlyInterest() + " por ciento mensual; si los pagos se realizan según lo pactado, "
                    + "no se agregará otro interés ordinario a dichas cuotas.");
        }
        if (!blank(terms.defaultInterest())) {
            provisions.add("En caso de mora, se aplicará un interés moratorio de "
                    + terms.defaultInterest() + " por ciento mensual sobre el capital vencido, "
                    + "sin exceder la tasa máxima legal vigente.");
        }
        return String.join(" ", provisions);
    }

    private String mutualType(com.big.dreamer.doccentral.document.mutual.model.MutualGuaranteeType guarantee) {
        return switch (guarantee) {
            case VEHICLE_PLEDGE -> "CONTRATO DE MUTUO CON GARANTÍA PRENDARIA";
            case MORTGAGE -> "CONTRATO DE MUTUO HIPOTECARIO";
            case PERSONAL_GUARANTOR -> "CONTRATO DE MUTUO CON GARANTÍA PERSONAL";
            default -> "CONTRATO DE MUTUO";
        };
    }

    private String formatDate(java.time.LocalDate value) {
        return LegalDocumentText.date(value);
    }

    private String money(BigDecimal value) {
        return LegalDocumentText.money(value);
    }

    private String person(Map<String, String> templates, PersonDetails person) {
        return render(templates, "person.txt", personValues(person));
    }

    private String identified(Map<String, String> templates, PersonDetails person, String known) {
        Map<String, String> values = new java.util.LinkedHashMap<>(personValues(person));
        values.put("knowledge", NotarialIdentificationText.knowledge(known));
        return render(templates, "identified-person.txt", values);
    }

    private Map<String, String> personValues(PersonDetails person) {
        return Map.ofEntries(
                Map.entry("name", fullName(person)),
                Map.entry("age", person.age()),
                Map.entry("job", person.job()),
                Map.entry("settlement", person.settlement()),
                Map.entry("state", person.state()),
                Map.entry("document", person.document()));
    }

    private String currency(String amount) {
        return LegalDocumentText.currencyFromWords(amount);
    }

    private String number(int value) {
        return switch (value) { case 1 -> "I)"; case 2 -> "II)"; case 3 -> "III)"; case 4 -> "IV)";
            case 5 -> "V)"; case 6 -> "VI)"; case 7 -> "VII)"; case 8 -> "VIII)";
            case 9 -> "IX)"; case 10 -> "X)"; case 11 -> "XI)"; default -> "XII)"; };
    }

    private boolean female(PersonDetails person) { return "Femenino".equalsIgnoreCase(person.gender()); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private String value(String value, String fallback) { return blank(value) ? fallback : value; }
    private String capitalize(String value) { return Character.toUpperCase(value.charAt(0)) + value.substring(1); }
    private String fullName(PersonDetails person) { return fullName(person.givenName(), person.lastName()); }
    private String fullName(String first, String last) { return (first + " " + last).trim().replaceAll("\\s+", " ").toUpperCase(Locale.forLanguageTag("es-SV")); }

}
