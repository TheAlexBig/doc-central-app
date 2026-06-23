package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
import com.big.dreamer.doccentral.document.history.model.CarSaleGenerationRequest;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
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

    public CarSaleDocumentController(
            CarSaleDocumentService documentService,
            GeneratedDocumentStorage documentStorage,
            GeneratedDocumentHistoryRepository historyRepository) {
        this.documentService = documentService;
        this.documentStorage = documentStorage;
        this.historyRepository = historyRepository;
    }

    @PostMapping(value = "/car-sale")
    public ResponseEntity<byte[]> generateCarSaleDocument(
            @Valid @RequestBody CarSaleDocumentRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        DocumentResponse document = createDocumentResponse(request, Instant.now(), normalizeFormat(format));
        return fileResponse(document.fileName(), document.contents(), document.contentType(), null);
    }

    @PostMapping(value = "/car-sale/history")
    public ResponseEntity<byte[]> generateTrackedCarSaleDocument(
            @Valid @RequestBody CarSaleGenerationRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        Instant createdAt = Instant.now();
        DocumentResponse document = createDocumentResponse(request.document(), createdAt, normalizeFormat(format));
        GeneratedDocumentMetadata metadata = historyRepository.saveCarSale(
                document.fileName(),
                createdAt.toString(),
                request.document(),
                request.draft());
        return fileResponse(document.fileName(), document.contents(), document.contentType(), metadata.id());
    }

    public byte[] createDocument(CarSaleDocumentRequest request, String format) {
        return PDF_FORMAT.equals(normalizeFormat(format))
                ? documentService.createPdfDocument(request)
                : documentService.createDocument(request);
    }

    public String contentType(String format) {
        return PDF_FORMAT.equals(normalizeFormat(format)) ? PDF_CONTENT_TYPE : WORD_CONTENT_TYPE;
    }

    public String fileName(Instant createdAt, String format) {
        return "compra-venta_" + FILE_CREATED_AT.format(createdAt) + "." + normalizeFormat(format);
    }

    public String normalizeFormat(String format) {
        return PDF_FORMAT.equalsIgnoreCase(format) ? PDF_FORMAT : WORD_FORMAT;
    }

    private DocumentResponse createDocumentResponse(
            CarSaleDocumentRequest request,
            Instant createdAt,
            String format) {
        return new DocumentResponse(
                fileName(createdAt, format),
                createDocument(request, format),
                contentType(format));
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
