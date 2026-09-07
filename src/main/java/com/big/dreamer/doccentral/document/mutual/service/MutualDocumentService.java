package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualTerms;
import com.big.dreamer.doccentral.document.mutual.template.MutualTemplateRepository;
import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import java.util.Map;
import java.math.BigDecimal;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MutualDocumentService {

    private static final float FONT_SIZE = 11;
    private static final float LINE_HEIGHT = 15;
    private static final float MARGIN = 54;
    private static final Pattern AMOUNT_WITH_CENTS = Pattern.compile("^(.+?) CON (.+ CENTAVOS)$");

    private final MutualTemplateRepository repository;
    private final MutualRulesService rules;

    public MutualDocumentService(MutualTemplateRepository repository, MutualRulesService rules) {
        this.repository = repository;
        this.rules = rules;
    }

    private String render(Map<String, String> templates, String name, Map<String, String> values) {
        return EditableTemplateRepository.render(templates.get(name), values);
    }

    public byte[] createDocument(MutualDocumentRequest request) {
        MutualDocumentContent content = assemble(request);
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addParagraph(document, content.contract());
            addSignatures(document, content);
            if (!blank(content.authentic())) {
                document.createParagraph().createRun().addBreak(BreakType.PAGE);
                addParagraph(document, content.authentic());
                addSignatures(document, content);
            }
            document.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the mutual agreement.", exception);
        }
    }

    public byte[] createPdfDocument(MutualDocumentRequest request) {
        MutualDocumentContent content = assemble(request);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.paragraph(content.contract());
            writer.signatures(content);
            if (!blank(content.authentic())) {
                writer.newPage();
                writer.paragraph(content.authentic());
                writer.signatures(content);
            }
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the mutual agreement PDF.", exception);
        }
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
                    Map.entry("periodicity", SpanishLegalText.replaceDigits(plan.periodicity())),
                    Map.entry("schedule", schedule(plan)))));
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

    private String schedule(MutualFinancialPlan plan) {
        return plan.installments().stream()
                .map(item -> SpanishLegalText.number(item.number()) + ") " + formatDate(item.dueDate())
                        + ": capital " + money(item.capital())
                        + ", interés " + money(item.interest())
                        + ", cuota " + money(item.total()))
                .reduce((left, right) -> left + "; " + right).orElse("");
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
        return SpanishLegalText.date(value);
    }

    private String money(BigDecimal value) {
        return SpanishLegalText.money(value);
    }

    private String person(Map<String, String> templates, PersonDetails person) {
        return render(templates, "person.txt", Map.ofEntries(
                Map.entry("name", fullName(person)),
                Map.entry("age", person.age()),
                Map.entry("job", person.job()),
                Map.entry("settlement", person.settlement()),
                Map.entry("state", person.state()),
                Map.entry("document", person.document())));
    }

    private String identified(Map<String, String> templates, PersonDetails person, String known) {
        return person(templates, person) + ("Sí".equalsIgnoreCase(known) ? ", a quien conozco" : ", a quien no conozco e identifico");
    }

    private String currency(String amount) {
        Matcher matcher = AMOUNT_WITH_CENTS.matcher(amount == null ? "" : amount);
        if (matcher.matches()) {
            return matcher.group(1) + " DÓLARES CON " + matcher.group(2)
                    + " DE DÓLAR DE LOS ESTADOS UNIDOS DE AMÉRICA";
        }
        return amount + " DÓLARES DE LOS ESTADOS UNIDOS DE AMÉRICA";
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

    private void addParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        paragraph.createRun().setText(text);
    }

    private void addSignatures(XWPFDocument document, MutualDocumentContent content) {
        int columns = blank(content.guarantorName()) ? 2 : 3;
        XWPFTable table = document.createTable(1, columns);
        table.removeBorders();
        table.setWidth("100%");
        signature(table.getRow(0).getCell(0).getParagraphs().getFirst(), content.debtorName(), content.debtorTitle());
        signature(table.getRow(0).getCell(1).getParagraphs().getFirst(), content.creditorName(), content.creditorTitle());
        if (columns == 3) {
            signature(table.getRow(0).getCell(2).getParagraphs().getFirst(),
                    content.guarantorName(), content.guarantorTitle());
        }
    }

    private void signature(XWPFParagraph paragraph, String name, String title) {
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        var run = paragraph.createRun();
        for (int index = 0; index < 4; index++) run.addBreak();
        run.setText(name); run.addBreak(); run.setText(title);
    }

    private static final class PdfWriter {
        private final PDDocument document;
        private final PDType1Font font = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
        private final PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
        private PDPageContentStream content;
        private float y;

        private PdfWriter(PDDocument document) throws IOException { this.document = document; newPage(); }
        private void newPage() throws IOException { if (content != null) content.close(); PDPage page = new PDPage(PDRectangle.LETTER); document.addPage(page); content = new PDPageContentStream(document, page); y = page.getMediaBox().getHeight() - MARGIN; }
        private void paragraph(String text) throws IOException { float width = PDRectangle.LETTER.getWidth() - MARGIN * 2; List<String> lines = wrap(text, width); for (int i = 0; i < lines.size(); i++) { space(LINE_HEIGHT); line(lines.get(i), width, i == lines.size() - 1); y -= LINE_HEIGHT; } y -= LINE_HEIGHT; }
        private void line(String text, float width, boolean last) throws IOException { int spaces = Math.max(0, text.split(" ").length - 1); float textWidth = font.getStringWidth(text) / 1000 * FONT_SIZE; content.beginText(); content.setFont(font, FONT_SIZE); content.setWordSpacing(!last && spaces > 0 ? (width - textWidth) / spaces : 0); content.newLineAtOffset(MARGIN, y); content.showText(text); content.endText(); }
        private void signatures(MutualDocumentContent data) throws IOException { space(LINE_HEIGHT * 7); y -= LINE_HEIGHT * 3; int count = data.guarantorName().isBlank() ? 2 : 3; float width = (PDRectangle.LETTER.getWidth() - MARGIN * 2) / count; centered(data.debtorName(), MARGIN, width, bold); centered(data.creditorName(), MARGIN + width, width, bold); if (count == 3) centered(data.guarantorName(), MARGIN + width * 2, width, bold); y -= LINE_HEIGHT; centered(data.debtorTitle(), MARGIN, width, font); centered(data.creditorTitle(), MARGIN + width, width, font); if (count == 3) centered(data.guarantorTitle(), MARGIN + width * 2, width, font); }
        private void centered(String text, float x, float width, PDType1Font selected) throws IOException { float textWidth = selected.getStringWidth(text) / 1000 * FONT_SIZE; content.beginText(); content.setFont(selected, FONT_SIZE); content.newLineAtOffset(x + (width - textWidth) / 2, y); content.showText(text); content.endText(); }
        private void space(float needed) throws IOException { if (y - needed < MARGIN) newPage(); }
        private List<String> wrap(String text, float width) throws IOException { List<String> lines = new ArrayList<>(); StringBuilder line = new StringBuilder(); for (String word : text.split("\\s+")) { String next = line.isEmpty() ? word : line + " " + word; if (font.getStringWidth(next) / 1000 * FONT_SIZE <= width) { line.setLength(0); line.append(next); } else { if (!line.isEmpty()) lines.add(line.toString()); line.setLength(0); line.append(word); } } if (!line.isEmpty()) lines.add(line.toString()); return lines; }
        private void close() throws IOException { if (content != null) content.close(); }
    }
}
