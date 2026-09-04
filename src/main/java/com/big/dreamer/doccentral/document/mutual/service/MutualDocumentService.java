package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualTerms;
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

    public byte[] createDocument(MutualDocumentRequest request) {
        MutualDocumentContent content = assemble(request);
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addParagraph(document, content.contract());
            addSignatures(document, content);
            document.createParagraph().createRun().addBreak(BreakType.PAGE);
            addParagraph(document, content.authentic());
            addSignatures(document, content);
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
            writer.newPage();
            writer.paragraph(content.authentic());
            writer.signatures(content);
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the mutual agreement PDF.", exception);
        }
    }

    MutualDocumentContent assemble(MutualDocumentRequest request) {
        PersonDetails debtor = request.debtor();
        PersonDetails creditor = request.creditor();
        MutualTerms terms = request.terms();
        String debtorRole = female(debtor) ? "LA DEUDORA" : "EL DEUDOR";
        String creditorRole = female(creditor) ? "LA ACREEDORA" : "EL ACREEDOR";
        String contract = "NOSOTROS: " + person(debtor) + ", que en lo sucesivo me denominaré \"" + debtorRole
                + "\"; y " + person(creditor) + ", que en adelante me denominaré \"" + creditorRole
                + "\", por medio del presente instrumento OTORGAMOS un CONTRATO DE MUTUO SIMPLE, sujeto a las siguientes cláusulas: "
                + clauses(request) + " En " + terms.signingPlace() + ", departamento de " + terms.signingState()
                + ", a " + terms.signingDate() + ".";
        String notary = fullName(request.legalAgent().givenName(), request.legalAgent().lastName());
        String notaryTitle = "Femenino".equalsIgnoreCase(request.legalAgent().gender()) ? "NOTARIA" : "NOTARIO";
        String authentic = "En " + terms.signingPlace() + ", departamento de " + terms.signingState() + ", a las "
                + terms.signingTime() + " de " + terms.signingDate() + ". Ante mí, " + notary + ", " + notaryTitle
                + ", del domicilio de " + request.legalAgent().settlement() + ", departamento de "
                + request.legalAgent().state() + ", comparecen " + identified(debtor, terms.identifiesDebtor())
                + ", denominado \"" + debtorRole + "\", y " + identified(creditor, terms.identifiesCreditor())
                + ", denominado \"" + creditorRole + "\". ME DICEN: Que reconocen como suyas las firmas puestas en el documento privado anterior y ratifican íntegramente sus declaraciones, obligaciones, pactos y renuncias, documento que literalmente dice: «"
                + contract + "» "
                + "YO, " + notaryTitle + ", DOY FE de que las firmas son auténticas por haber sido puestas en mi presencia. Advertí a los otorgantes lo dispuesto en el artículo doscientos veinte del Código Tributario, la Ley Contra el Lavado de Dinero y de Activos y la Ley Contra la Usura. Expliqué los efectos legales de esta acta notarial y, leída íntegramente en un solo acto sin interrupción, ratifican su contenido y firmamos. DOY FE.";
        return new MutualDocumentContent(contract, authentic, fullName(debtor), fullName(creditor), debtorRole, creditorRole);
    }

    private String clauses(MutualDocumentRequest request) {
        MutualTerms terms = request.terms();
        PersonDetails debtor = request.debtor();
        PersonDetails creditor = request.creditor();
        String debtorText = female(debtor) ? "la deudora" : "el deudor";
        String creditorText = female(creditor) ? "la acreedora" : "el acreedor";
        String fromCreditor = female(creditor) ? "de la acreedora" : "del acreedor";
        String forCreditor = female(creditor) ? "a favor de la acreedora" : "a favor del acreedor";
        String installments = "UNO".equalsIgnoreCase(terms.installmentCount())
                || "UNA".equalsIgnoreCase(terms.installmentCount())
                ? "UNA CUOTA"
                : terms.installmentCount() + " CUOTAS";
        List<String> clauses = new ArrayList<>();
        clauses.add("I) MUTUO: " + capitalize(debtorText) + " recibe a su entera satisfacción " + fromCreditor
                + " la cantidad de " + currency(terms.amount()) + ".");
        clauses.add("II) PLAZO Y FORMA DE PAGO: La suma mutuada será cancelada en un plazo de " + terms.term()
                + ", con vencimiento el " + terms.dueDate() + ", mediante " + installments
                + " de " + currency(terms.installmentAmount()) + ". El pago se depositará en la cuenta "
                + terms.paymentAccount() + " del " + terms.paymentBank() + ", a nombre de " + fullName(creditor) + ".");
        if (!blank(terms.monthlyInterest()) || !blank(terms.defaultInterest())) {
            clauses.add("III) INTERESES: La suma mutuada devengará " + value(terms.monthlyInterest(), "cero")
                    + " por ciento de interés mensual y, en caso de mora, " + value(terms.defaultInterest(), "cero")
                    + " por ciento mensual adicional, sin exceder la tasa máxima legal vigente.");
        }
        clauses.add(number(clauses.size() + 1) + " ORIGEN Y DESTINO DE LOS FONDOS: El crédito se otorga con fondos propios obtenidos lícitamente. "
                + capitalize(debtorText) + " destinará los fondos a " + terms.fundsPurpose()
                + " y pagará la obligación con fondos procedentes de actividades lícitas.");
        if (terms.billOfExchangeGuarantee()) {
            clauses.add(number(clauses.size() + 1) + " GARANTÍA: " + capitalize(debtorText)
                    + " suscribe una letra de cambio sin protesto por " + currency(terms.amount()) + ", "
                    + forCreditor + ", con vencimiento el " + terms.guaranteeDueDate()
                    + ". La letra garantiza esta obligación y no constituye una doble obligación.");
        }
        clauses.add(number(clauses.size() + 1) + " CAUSALES DE CADUCIDAD: El plazo caducará y podrá exigirse la totalidad de la deuda por ejecución promovida contra "
                + debtorText + " o por incumplimiento de cualquiera de las condiciones de este contrato.");
        clauses.add(number(clauses.size() + 1) + " CADUCIDAD DEL PLAZO: La obligación se tendrá por vencida y será exigible en su totalidad en caso de mora o incumplimiento.");
        String expenses = blank(terms.administrativeExpenses()) ? "" : " " + capitalize(debtorText)
                + " pagará " + terms.administrativeExpenses() + " por ciento en concepto de gastos administrativos.";
        clauses.add(number(clauses.size() + 1) + " DOMICILIO Y RENUNCIAS: Para los efectos legales, " + debtorText
                + " señala como domicilio especial " + terms.specialDomicile() + ", a cuyos tribunales se somete expresamente. Los gastos judiciales y extrajudiciales ocasionados por este adeudo serán por cuenta de "
                + debtorText + "." + expenses);
        return String.join(" ", clauses);
    }

    private String person(PersonDetails person) {
        return fullName(person) + ", de " + person.age() + " años de edad, " + person.job() + ", del domicilio de "
                + person.settlement() + ", departamento de " + person.state()
                + ", con Documento Único de Identidad homologado número " + person.document();
    }

    private String identified(PersonDetails person, String known) {
        return person(person) + ("Sí".equalsIgnoreCase(known) ? ", a quien conozco" : ", a quien no conozco e identifico");
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
            case 5 -> "V)"; case 6 -> "VI)"; case 7 -> "VII)"; default -> "VIII)"; };
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
        XWPFTable table = document.createTable(1, 2);
        table.removeBorders();
        table.setWidth("100%");
        signature(table.getRow(0).getCell(0).getParagraphs().getFirst(), content.debtorName(), content.debtorTitle());
        signature(table.getRow(0).getCell(1).getParagraphs().getFirst(), content.creditorName(), content.creditorTitle());
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
        private void signatures(MutualDocumentContent data) throws IOException { space(LINE_HEIGHT * 7); y -= LINE_HEIGHT * 3; float width = (PDRectangle.LETTER.getWidth() - MARGIN * 2) / 2; centered(data.debtorName(), MARGIN, width, bold); centered(data.creditorName(), MARGIN + width, width, bold); y -= LINE_HEIGHT; centered(data.debtorTitle(), MARGIN, width, font); centered(data.creditorTitle(), MARGIN + width, width, font); }
        private void centered(String text, float x, float width, PDType1Font selected) throws IOException { float textWidth = selected.getStringWidth(text) / 1000 * FONT_SIZE; content.beginText(); content.setFont(selected, FONT_SIZE); content.newLineAtOffset(x + (width - textWidth) / 2, y); content.showText(text); content.endText(); }
        private void space(float needed) throws IOException { if (y - needed < MARGIN) newPage(); }
        private List<String> wrap(String text, float width) throws IOException { List<String> lines = new ArrayList<>(); StringBuilder line = new StringBuilder(); for (String word : text.split("\\s+")) { String next = line.isEmpty() ? word : line + " " + word; if (font.getStringWidth(next) / 1000 * FONT_SIZE <= width) { line.setLength(0); line.append(next); } else { if (!line.isEmpty()) lines.add(line.toString()); line.setLength(0); line.append(word); } } if (!line.isEmpty()) lines.add(line.toString()); return lines; }
        private void close() throws IOException { if (content != null) content.close(); }
    }
}
