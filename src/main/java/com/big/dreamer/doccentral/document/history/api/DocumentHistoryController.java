package com.big.dreamer.doccentral.document.history.api;

import com.big.dreamer.doccentral.document.carsale.api.CarSaleDocumentController;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/documents/history")
public class DocumentHistoryController {

    private final GeneratedDocumentHistoryRepository historyRepository;
    private final GeneratedDocumentStorage documentStorage;
    private final CarSaleDocumentService documentService;

    public DocumentHistoryController(
            GeneratedDocumentHistoryRepository historyRepository,
            GeneratedDocumentStorage documentStorage,
            CarSaleDocumentService documentService) {
        this.historyRepository = historyRepository;
        this.documentStorage = documentStorage;
        this.documentService = documentService;
    }

    @GetMapping
    public List<GeneratedDocumentMetadata> listHistory() {
        return historyRepository.findAll();
    }

    @GetMapping("/{id}")
    public GeneratedDocumentMetadata getHistoryItem(@PathVariable String id) {
        return historyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Document history item not found."));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadHistoryFile(
            @PathVariable String id,
            @RequestParam(defaultValue = CarSaleDocumentController.WORD_FORMAT) String format) {
        GeneratedDocumentMetadata metadata = getHistoryItem(id);
        String normalizedFormat = normalizeFormat(format);
        String fileName = fileName(metadata, normalizedFormat);
        byte[] document = documentStorage.read(fileName)
                .orElseGet(() -> createDocument(metadata, normalizedFormat));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType(normalizedFormat)))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(document);
    }

    private byte[] createDocument(GeneratedDocumentMetadata metadata, String format) {
        return CarSaleDocumentController.PDF_FORMAT.equals(format)
                ? documentService.createPdfDocument(metadata.document())
                : documentService.createDocument(metadata.document());
    }

    private String fileName(GeneratedDocumentMetadata metadata, String format) {
        if (metadata.fileName() != null && metadata.fileName().endsWith("." + format)) {
            return metadata.fileName();
        }
        return "compra-venta_" + DateFormats.FILE_CREATED_AT.format(Instant.parse(metadata.createdAt())) + "." + format;
    }

    private String contentType(String format) {
        return CarSaleDocumentController.PDF_FORMAT.equals(format)
                ? CarSaleDocumentController.PDF_CONTENT_TYPE
                : CarSaleDocumentController.WORD_CONTENT_TYPE;
    }

    private String normalizeFormat(String format) {
        return CarSaleDocumentController.PDF_FORMAT.equalsIgnoreCase(format)
                ? CarSaleDocumentController.PDF_FORMAT
                : CarSaleDocumentController.WORD_FORMAT;
    }

    private static final class DateFormats {
        private static final java.time.format.DateTimeFormatter FILE_CREATED_AT =
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")
                        .withZone(java.time.ZoneOffset.UTC);
    }
}
