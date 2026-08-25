package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidator;
import com.big.dreamer.doccentral.document.history.model.CarSaleGenerationRequest;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import com.big.dreamer.doccentral.license.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class CarSaleDocumentController {

    public static final String WORD_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String WORD_FORMAT = "docx";
    public static final String PDF_FORMAT = "pdf";
    private static final DateTimeFormatter FILE_CREATED_AT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")
                    .withZone(ZoneOffset.UTC);

    private final CarSaleDocumentService documentService;
    private final GeneratedDocumentStorage documentStorage;
    private final GeneratedDocumentHistoryRepository historyRepository;
    private final LicenseService licenseService;

    public CarSaleDocumentController(
            CarSaleDocumentService documentService,
            GeneratedDocumentStorage documentStorage,
            GeneratedDocumentHistoryRepository historyRepository,
            LicenseService licenseService) {
        this.documentService = documentService;
        this.documentStorage = documentStorage;
        this.historyRepository = historyRepository;
        this.licenseService = licenseService;
    }

    @PostMapping(value = "/car-sale")
    public ResponseEntity<byte[]> generateCarSaleDocument(
            @Valid @RequestBody CarSaleDocumentRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        licenseService.requireActive();
        DocumentResponse document = createDocumentResponse(
                request, Instant.now(), DocumentFormat.from(format), request.vehicle().licensePlate());
        return fileResponse(document.fileName(), document.contents(), document.contentType(), null);
    }

    @PostMapping(value = "/car-sale/history")
    public ResponseEntity<byte[]> generateTrackedCarSaleDocument(
            @Valid @RequestBody CarSaleGenerationRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        licenseService.requireActive();
        Instant createdAt = Instant.now();
        DocumentResponse document = createDocumentResponse(
                request.document(), createdAt, DocumentFormat.from(format), draftLicensePlate(request));
        GeneratedDocumentMetadata metadata = historyRepository.saveCarSale(
                document.fileName(),
                createdAt.toString(),
                request.document(),
                request.draft());
        return fileResponse(document.fileName(), document.contents(), document.contentType(), metadata.id());
    }

    public byte[] createDocument(CarSaleDocumentRequest request, String format) {
        return DocumentFormat.from(format).isPdf()
                ? documentService.createPdfDocument(request)
                : documentService.createDocument(request);
    }

    public String contentType(String format) {
        return DocumentFormat.from(format).contentType();
    }

    public String fileName(Instant createdAt, String format) {
        return fileName(createdAt, DocumentFormat.from(format));
    }

    private DocumentResponse createDocumentResponse(
            CarSaleDocumentRequest request,
            Instant createdAt,
            DocumentFormat format,
            String licensePlate) {
        return new DocumentResponse(
                fileName(createdAt, format, licensePlate),
                createDocument(request, format),
                format.contentType());
    }

    private byte[] createDocument(CarSaleDocumentRequest request, DocumentFormat format) {
        CarSaleRequestValidator.validate(request);
        return format.isPdf()
                ? documentService.createPdfDocument(request)
                : documentService.createDocument(request);
    }

    private String fileName(Instant createdAt, DocumentFormat format) {
        return "compra-venta_" + FILE_CREATED_AT.format(createdAt) + "." + format.extension();
    }

    private String fileName(Instant createdAt, DocumentFormat format, String licensePlate) {
        String safePlate = licensePlate == null
                ? ""
                : licensePlate.trim().replaceAll("[^\\p{L}\\p{N}-]+", "-");
        String platePart = safePlate.isBlank() ? "" : safePlate + "_";
        return "compra-venta_" + platePart + FILE_CREATED_AT.format(createdAt) + "." + format.extension();
    }

    private String draftLicensePlate(CarSaleGenerationRequest request) {
        Object vehicle = request.draft() == null ? null : request.draft().get("carStates");
        if (vehicle instanceof Map<?, ?> vehicleData) {
            Object plate = vehicleData.get("placa");
            if (plate != null && !plate.toString().isBlank()) {
                return plate.toString();
            }
        }
        return request.document().vehicle().licensePlate();
    }

    private ResponseEntity<byte[]> fileResponse(
            String fileName,
            byte[] document,
            String contentType,
            String historyId) {
        documentStorage.save(fileName, document);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        var response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        if (historyId != null) {
            response.header("X-Document-History-Id", historyId);
        }
        return response.body(document);
    }

    private record DocumentResponse(String fileName, byte[] contents, String contentType) {
    }
}
