package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class CarSaleDocumentService {

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
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            createDeclarationSection(document, request, templates);
            createSignatures(document, request.buyer(), request.seller());
            createPageBreak(document);
            createAuthenticSection(document, request, templates);
            createSignatures(document, request.buyer(), request.seller());
            document.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to generate the Word document.", exception);
        }
    }

    private void createDeclarationSection(
            XWPFDocument document,
            CarSaleDocumentRequest request,
            CarSaleTemplateRepository.Templates templates) {
        XWPFParagraph paragraph = justifiedParagraph(document);
        String people = populatePeople(
                templates.peopleDocument(),
                request.seller(),
                sellerTitle(request.seller()),
                request.buyer(),
                buyerTitle(request.buyer()));
        String car = populateCar(templates.carDocument(), request.vehicle());
        String documentTerms = populateDocument(
                templates.document() + templates.firstSectionEnd(),
                request.document());
        paragraph.createRun().setText(people + car + documentTerms);
    }

    private void createAuthenticSection(
            XWPFDocument document,
            CarSaleDocumentRequest request,
            CarSaleTemplateRepository.Templates templates) {
        XWPFParagraph paragraph = justifiedParagraph(document);
        String legalAgent = populateLegalAgent(templates.legalAuthentic(), request.legalAgent(), request.document());
        String people = populatePeople(
                templates.peopleAuthentic(),
                request.seller(),
                sellerTitle(request.seller()),
                request.buyer(),
                buyerTitle(request.buyer()));
        people = replaceFirst(people, ":identifiesSeller", identificationText(request.document().identifiesSeller()));
        people = replaceFirst(people, ":identifiesBuyer", identificationText(request.document().identifiesBuyer()));
        String car = populateCar(templates.carAuthentic(), request.vehicle());
        String documentTerms = populateDocument(
                templates.document() + templates.secondSectionEnd(),
                request.document());
        paragraph.createRun().setText(legalAgent + people + car + documentTerms);
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
}
