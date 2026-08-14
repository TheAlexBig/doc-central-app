package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;
import com.big.dreamer.doccentral.document.history.api.DocumentHistoryController;
import com.big.dreamer.doccentral.document.history.model.CarSaleGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CarSaleDocumentControllerTests {

    @TempDir
    Path temporaryDirectory;

    private CarSaleDocumentController documentController;
    private DocumentHistoryController historyController;
    private GeneratedDocumentHistoryRepository historyRepository;

    @BeforeEach
    void setUp() {
        ApplicationDirectories directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        CarSaleTemplateRepository templateRepository = new CarSaleTemplateRepository(directories);
        templateRepository.initializeTemplates();
        CarSaleDocumentService documentService = new CarSaleDocumentService(templateRepository);
        GeneratedDocumentStorage documentStorage = new GeneratedDocumentStorage(directories);
        historyRepository = new GeneratedDocumentHistoryRepository(directories, new ObjectMapper());
        historyRepository.initialize();
        documentController = new CarSaleDocumentController(documentService, documentStorage, historyRepository);
        historyController = new DocumentHistoryController(historyRepository, documentStorage, documentService);
    }

    @Test
    void generatesTrackedPdfAndCanDownloadHistoricalDocx() {
        ResponseEntity<byte[]> pdfResponse = documentController.generateTrackedCarSaleDocument(
                new CarSaleGenerationRequest(
                        carSaleRequest(), Map.of("carStates", Map.of("placa", "C-987654"))),
                "pdf");

        assertThat(pdfResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(pdfResponse.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(pdfResponse.getHeaders().getContentDisposition().getFilename()).endsWith(".pdf");
        assertThat(pdfResponse.getHeaders().getContentDisposition().getFilename()).contains("C-987654");
        assertThat(pdfResponse.getBody()).isNotEmpty();

        String historyId = pdfResponse.getHeaders().getFirst("X-Document-History-Id");
        assertThat(historyId).isNotBlank();
        assertThat(historyRepository.findAll())
                .singleElement()
                .satisfies(metadata -> {
                    assertThat(metadata.id()).isEqualTo(historyId);
                    assertThat(metadata.title()).isEqualTo("Compra venta - Toyota Corolla P-123");
                    assertThat(metadata.buyerName()).isEqualTo("Carlos Perez");
                    assertThat(metadata.sellerName()).isEqualTo("Maria Lopez");
                });

        ResponseEntity<byte[]> docxResponse = historyController.downloadHistoryFile(historyId, "docx");

        assertThat(docxResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(docxResponse.getHeaders().getContentType())
                .isEqualTo(MediaType.parseMediaType(CarSaleDocumentController.WORD_CONTENT_TYPE));
        assertThat(docxResponse.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains(".docx");
        assertThat(docxResponse.getBody()).isNotEmpty();
    }

    private CarSaleDocumentRequest carSaleRequest() {
        return new CarSaleDocumentRequest(
                new PersonDetails(
                        "Maria",
                        "Lopez",
                        "San Salvador",
                        "San Salvador",
                        "12345678-9",
                        "Femenino",
                        "TREINTA Y CINCO",
                        "Abogada"),
                new PersonDetails(
                        "Carlos",
                        "Perez",
                        "La Libertad",
                        "Santa Tecla",
                        "87654321-0",
                        "Masculino",
                        "TREINTA",
                        "Ingeniero"),
                new CarDetails(
                        "P-123",
                        "Toyota",
                        "Corolla",
                        "Azul",
                        "DOS MIL VEINTE",
                        "CINCO ASS",
                        "Propiedad",
                        "Automóvil",
                        "Sedán",
                        "MOTOR1",
                        "CHASIS1",
                        "VIN1"),
                new DocumentDetails(
                        "Propiedad",
                        "",
                        "DIEZ MIL",
                        "Santa Tecla",
                        "La Libertad",
                        "VEINTISEIS DE MAYO DE DOS MIL VEINTISEIS",
                        "DIEZ HORAS",
                        "No",
                        "Si"),
                new LegalAgentDetails(
                        "Ana",
                        "Garcia",
                        "La Libertad",
                        "Santa Tecla",
                        "Femenino",
                        "Notario"));
    }
}
