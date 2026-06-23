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

    @PostMapping(value = "/car-sale", produces = WORD_CONTENT_TYPE)
    public ResponseEntity<byte[]> generateCarSaleDocument(
            @Valid @RequestBody CarSaleDocumentRequest request) {
        byte[] document = documentService.createDocument(request);
        return wordResponse(fileName(Instant.now()), document, null);
    }

    @PostMapping(value = "/car-sale/history", produces = WORD_CONTENT_TYPE)
    public ResponseEntity<byte[]> generateTrackedCarSaleDocument(
            @Valid @RequestBody CarSaleGenerationRequest request) {
        Instant createdAt = Instant.now();
        byte[] document = documentService.createDocument(request.document());
        String fileName = fileName(createdAt);
        GeneratedDocumentMetadata metadata = historyRepository.saveCarSale(
                fileName,
                createdAt.toString(),
                request.document(),
                request.draft());
        return wordResponse(fileName, document, metadata.id());
    }

    private String fileName(Instant createdAt) {
        return "compra-venta_" + FILE_CREATED_AT.format(createdAt) + ".docx";
    }

    private ResponseEntity<byte[]> wordResponse(String fileName, byte[] document, String historyId) {
        documentStorage.save(fileName, document);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        var response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(WORD_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString());
        if (historyId != null) {
            response.header("X-Document-History-Id", historyId);
        }
        return response.body(document);
    }
}
