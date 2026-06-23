package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;
import jakarta.annotation.PostConstruct;
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
import java.util.List;

@Service
public class CarSaleDocumentService {

    private static final PDType1Font PDF_FONT = new PDType1Font(Standard14Fonts.FontName.TIMES_ROMAN);
    private static final PDType1Font PDF_BOLD_FONT = new PDType1Font(Standard14Fonts.FontName.TIMES_BOLD);
    private static final float PDF_FONT_SIZE = 11.0f;
    private static final float PDF_LINE_HEIGHT = 15.0f;
    private static final float PDF_MARGIN = 54.0f;
    private static final String BUYER_DEFAULT = "EL COMPRADOR";
    private static final String BUYER_WOMAN = "LA COMPRADORA";
    private static final String SELLER_DEFAULT = "EL VENDEDOR";
    private static final String SELLER_WOMAN = "LA VENDEDORA";
    private static final String LEGAL_DEFAULT = "NOTARIO";
    private static final String LEGAL_WOMAN = "NOTARIA";
    private static final String LAWYER_DEFAULT = "ABOGADO";
    private static final String LAWYER_WOMAN = "ABOGADA";
    private static final CarSaleDocumentRequest WARM_UP_REQUEST = new CarSaleDocumentRequest(
            new PersonDetails("Inicial", "Vendedor", "Departamento", "Municipio",
                    "00000000-0", "Masculino", "30", "Oficio"),
            new PersonDetails("Inicial", "Comprador", "Departamento", "Municipio",
                    "00000000-0", "Masculino", "30", "Oficio"),
            new CarDetails("P-000", "Marca", "Modelo", "Color", "2026", "5",
                    "Propiedad", "Clase", "MOTOR", "CHASIS", "VIN"),
            new DocumentDetails("Propiedad", "", "PRECIO", "Municipio",
                    "Departamento", "FECHA", "HORA", "No", "No"),
            new LegalAgentDetails("Inicial", "Notario", "Departamento", "Municipio", "Masculino", "Notario"));
    private final CarSaleTemplateRepository templateRepository;

    public CarSaleDocumentService(CarSaleTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    void initializeDocumentWriter() {
        createDocument(WARM_UP_REQUEST);
    }

    public byte[] createDocument(CarSaleDocumentRequest request) {
        CarSaleTemplateRepository.Templates templates = templateRepository.load();
        DocumentSections sections = createSections(request, templates);
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            createDeclarationSection(document, sections.declaration());
            createSignatures(document, request.buyer(), request.seller());
            createPageBreak(document);
            createAuthenticSection(document, sections.authentic());
            createSignatures(document, request.buyer(), request.seller());
            document.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the Word document.", exception);
        }
    }

    public byte[] createPdfDocument(CarSaleDocumentRequest request) {
        CarSaleTemplateRepository.Templates templates = templateRepository.load();
        DocumentSections sections = createSections(request, templates);
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(document);
            writer.writeParagraph(sections.declaration());
            writer.writeSignatures(request.buyer(), request.seller(), buyerTitle(request.buyer()), sellerTitle(request.seller()));
            writer.newPage();
            writer.writeParagraph(sections.authentic());
            writer.writeSignatures(request.buyer(), request.seller(), buyerTitle(request.buyer()), sellerTitle(request.seller()));
            writer.close();
            document.save(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the PDF document.", exception);
        }
    }

    private DocumentSections createSections(
            CarSaleDocumentRequest request,
            CarSaleTemplateRepository.Templates templates) {
        String declarationPeople = populatePeople(
                templates.peopleDocument(),
                request.seller(),
                sellerTitle(request.seller()),
                request.buyer(),
                buyerTitle(request.buyer()));
        String declarationCar = populateCar(templates.carDocument(), request.vehicle());
        String declarationTerms = populateDocument(
                templates.document() + templates.firstSectionEnd(),
                request.document());

        String legalAgent = populateLegalAgent(templates.legalAuthentic(), request.legalAgent(), request.document());
        String authenticPeople = populatePeople(
                templates.peopleAuthentic(),
                request.seller(),
                sellerTitle(request.seller()),
                request.buyer(),
                buyerTitle(request.buyer()));
        authenticPeople = replaceFirst(
                authenticPeople,
                ":identifiesSeller",
                identificationText(request.document().identifiesSeller()));
        authenticPeople = replaceFirst(
                authenticPeople,
                ":identifiesBuyer",
                identificationText(request.document().identifiesBuyer()));
        String authenticCar = populateCar(templates.carAuthentic(), request.vehicle());
        String authenticTerms = populateDocument(
                templates.document() + templates.secondSectionEnd(),
                request.document());
        return new DocumentSections(
                declarationPeople + declarationCar + declarationTerms,
                legalAgent + authenticPeople + authenticCar + authenticTerms);
    }

    private void createDeclarationSection(XWPFDocument document, String declaration) {
        XWPFParagraph paragraph = justifiedParagraph(document);
        paragraph.createRun().setText(declaration);
    }

    private void createAuthenticSection(XWPFDocument document, String authentic) {
        XWPFParagraph paragraph = justifiedParagraph(document);
        paragraph.createRun().setText(authentic);
    }

    private XWPFParagraph justifiedParagraph(XWPFDocument document) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.BOTH);
        return paragraph;
    }

    private String populatePeople(String template, PersonDetails first, String firstTitle,
                                  PersonDetails second, String secondTitle) {
        String populated = populatePerson(template, first, firstTitle);
        return populatePerson(populated, second, secondTitle);
    }

    private String populatePerson(String template, PersonDetails person, String title) {
        String populated = replaceFirst(template, ":givenName", person.givenName());
        populated = replaceFirst(populated, ":lastName", person.lastName());
        populated = replaceFirst(populated, ":age", person.age());
        populated = replaceFirst(populated, ":job", person.job());
        populated = replaceFirst(populated, ":settlement", person.settlement());
        populated = replaceFirst(populated, ":state", person.state());
        populated = replaceFirst(populated, ":document", person.document());
        return replaceFirst(populated, ":gender", title);
    }

    private String populateCar(String template, CarDetails car) {
        String populated = replaceFirst(template, ":licensePlate", car.licensePlate());
        populated = replaceFirst(populated, ":brand", car.brand());
        populated = replaceFirst(populated, ":model", car.model());
        populated = replaceFirst(populated, ":color", car.color());
        populated = replaceFirst(populated, ":factoryYear", car.factoryYear());
        populated = replaceFirst(populated, ":capacity", car.capacity());
        populated = replaceFirst(populated, ":domain", car.domain());
        populated = replaceFirst(populated, ":vehicleClass", car.vehicleClass());
        populated = replaceFirst(populated, ":engineNumber", car.engineNumber());
        populated = replaceFirst(populated, ":chassisNumber", car.chassisNumber());
        return replaceFirst(populated, ":vinNumber", car.vinNumber());
    }

    private String populateDocument(String template, DocumentDetails details) {
        String populated = replaceFirst(template, ":garment", details.garment());
        String institution = details.institution() == null || details.institution().isBlank()
                ? ""
                : "con " + details.institution();
        populated = replaceFirst(populated, ":institution", institution);
        populated = replaceFirst(populated, ":price", details.price());
        populated = replaceFirst(populated, ":settlement", details.settlement());
        populated = replaceFirst(populated, ":state", details.state());
        return replaceFirst(populated, ":signDate", details.signDate());
    }

    private String populateLegalAgent(String template, LegalAgentDetails agent, DocumentDetails details) {
        String populated = replaceFirst(template, ":settlement", details.settlement());
        populated = replaceFirst(populated, ":state", details.state());
        populated = replaceFirst(populated, ":signHour", details.signHour());
        populated = replaceFirst(populated, ":signDate", details.signDate());
        populated = replaceFirst(populated, ":givenName", agent.givenName());
        populated = replaceFirst(populated, ":lastName", agent.lastName());
        populated = replaceFirst(populated, ":gender", legalAgentTitle(agent));
        populated = replaceFirst(populated, ":settlement", agent.settlement());
        return replaceFirst(populated, ":state", agent.state());
    }

    private String legalAgentTitle(LegalAgentDetails agent) {
        boolean female = isFemale(agent.gender());
        if ("abogado".equalsIgnoreCase(agent.role())) {
            return female ? LAWYER_WOMAN : LAWYER_DEFAULT;
        }
        return female ? LEGAL_WOMAN : LEGAL_DEFAULT;
    }

    private void createSignatures(XWPFDocument document, PersonDetails buyer, PersonDetails seller) {
        XWPFTable table = document.createTable(1, 2);
        table.getCTTbl().getTblPr().unsetTblBorders();
        table.removeBorders();
        table.setWidth("100%");
        createSignature(table.getRow(0).getCell(0).getParagraphs().getFirst(), buyer, buyerTitle(buyer));
        createSignature(table.getRow(0).getCell(1).getParagraphs().getFirst(), seller, sellerTitle(seller));
        document.createParagraph().createRun().addBreak();
    }

    private void createSignature(XWPFParagraph paragraph, PersonDetails person, String title) {
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        var run = paragraph.createRun();
        run.addBreak();
        run.addBreak();
        run.addBreak();
        run.addBreak();
        run.setText(person.givenName() + " " + person.lastName());
        run.addBreak();
        run.setText(title);
    }

    private void createPageBreak(XWPFDocument document) {
        document.createParagraph().createRun().addBreak(BreakType.PAGE);
    }

    private String buyerTitle(PersonDetails buyer) {
        return isFemale(buyer.gender()) ? BUYER_WOMAN : BUYER_DEFAULT;
    }

    private String sellerTitle(PersonDetails seller) {
        return isFemale(seller.gender()) ? SELLER_WOMAN : SELLER_DEFAULT;
    }

    private boolean isFemale(String gender) {
        return "femenino".equalsIgnoreCase(gender);
    }

    private String identificationText(String identified) {
        return "No".equalsIgnoreCase(identified) ? "a quien no conozco" : "a quien hoy conozco";
    }

    private String replaceFirst(String source, String placeholder, String value) {
        int position = source.indexOf(placeholder);
        if (position < 0) {
            return source;
        }
        return source.substring(0, position) + value + source.substring(position + placeholder.length());
    }

    private record DocumentSections(String declaration, String authentic) {
    }

    private static final class PdfWriter {

        private final PDDocument document;
        private PDPageContentStream content;
        private float y;

        private PdfWriter(PDDocument document) throws IOException {
            this.document = document;
            newPage();
        }

        private void newPage() throws IOException {
            if (content != null) {
                content.close();
            }
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = page.getMediaBox().getHeight() - PDF_MARGIN;
        }

        private void writeParagraph(String text) throws IOException {
            for (String line : wrap(text, PDRectangle.LETTER.getWidth() - (PDF_MARGIN * 2))) {
                ensureSpace(PDF_LINE_HEIGHT);
                content.beginText();
                content.setFont(PDF_FONT, PDF_FONT_SIZE);
                content.newLineAtOffset(PDF_MARGIN, y);
                content.showText(line);
                content.endText();
                y -= PDF_LINE_HEIGHT;
            }
            y -= PDF_LINE_HEIGHT;
        }

        private void writeSignatures(
                PersonDetails buyer,
                PersonDetails seller,
                String buyerTitle,
                String sellerTitle) throws IOException {
            ensureSpace(PDF_LINE_HEIGHT * 7);
            y -= PDF_LINE_HEIGHT * 3;
            float columnWidth = (PDRectangle.LETTER.getWidth() - (PDF_MARGIN * 2)) / 2;
            writeCentered(buyer.givenName() + " " + buyer.lastName(), PDF_MARGIN, columnWidth, PDF_BOLD_FONT);
            writeCentered(seller.givenName() + " " + seller.lastName(), PDF_MARGIN + columnWidth, columnWidth, PDF_BOLD_FONT);
            y -= PDF_LINE_HEIGHT;
            writeCentered(buyerTitle, PDF_MARGIN, columnWidth, PDF_FONT);
            writeCentered(sellerTitle, PDF_MARGIN + columnWidth, columnWidth, PDF_FONT);
            y -= PDF_LINE_HEIGHT * 2;
        }

        private void writeCentered(String text, float x, float width, PDType1Font font) throws IOException {
            float textWidth = font.getStringWidth(text) / 1000 * PDF_FONT_SIZE;
            content.beginText();
            content.setFont(font, PDF_FONT_SIZE);
            content.newLineAtOffset(x + ((width - textWidth) / 2), y);
            content.showText(text);
            content.endText();
        }

        private void ensureSpace(float needed) throws IOException {
            if (y - needed < PDF_MARGIN) {
                newPage();
            }
        }

        private void close() throws IOException {
            if (content != null) {
                content.close();
                content = null;
            }
        }

        private List<String> wrap(String text, float maxWidth) throws IOException {
            List<String> lines = new java.util.ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : text.replace('\n', ' ').split("\\s+")) {
                if (word.isBlank()) {
                    continue;
                }
                String next = line.length() == 0 ? word : line + " " + word;
                float nextWidth = PDF_FONT.getStringWidth(next) / 1000 * PDF_FONT_SIZE;
                if (nextWidth <= maxWidth) {
                    line.setLength(0);
                    line.append(next);
                } else {
                    if (line.length() > 0) {
                        lines.add(line.toString());
                    }
                    line.setLength(0);
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }
            return lines;
        }
    }
}
