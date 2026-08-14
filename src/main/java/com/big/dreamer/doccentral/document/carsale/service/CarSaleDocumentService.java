package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class CarSaleDocumentService {

    private static final CarSaleDocumentRequest WARM_UP_REQUEST = new CarSaleDocumentRequest(
            new PersonDetails("Inicial", "Vendedor", "Departamento", "Municipio",
                    "00000000-0", "Masculino", "30", "Oficio"),
            new PersonDetails("Inicial", "Comprador", "Departamento", "Municipio",
                    "00000000-0", "Masculino", "30", "Oficio"),
            new CarDetails("P-000", "Marca", "Modelo", "Color", "2026", "CINCO ASS",
                    "Propiedad", "Automóvil", "Sedán", "MOTOR", "CHASIS", "VIN"),
            new DocumentDetails("Propiedad", "", "PRECIO", "Municipio",
                    "Departamento", "FECHA", "HORA", "No", "No"),
            new LegalAgentDetails("Inicial", "Notario", "Departamento", "Municipio", "Masculino", "Notario"));

    private final CarSaleTemplateRepository templateRepository;
    private final CarSaleDocumentAssembler assembler = new CarSaleDocumentAssembler();
    private final CarSaleWordDocumentRenderer wordRenderer = new CarSaleWordDocumentRenderer();
    private final CarSalePdfDocumentRenderer pdfRenderer = new CarSalePdfDocumentRenderer();

    public CarSaleDocumentService(CarSaleTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @PostConstruct
    void initializeDocumentWriter() {
        createDocument(WARM_UP_REQUEST);
    }

    public byte[] createDocument(CarSaleDocumentRequest request) {
        return wordRenderer.render(createSections(request));
    }

    public byte[] createPdfDocument(CarSaleDocumentRequest request) {
        return pdfRenderer.render(createSections(request));
    }

    private CarSaleDocumentSections createSections(CarSaleDocumentRequest request) {
        return assembler.createSections(request, templateRepository.load());
    }
}
