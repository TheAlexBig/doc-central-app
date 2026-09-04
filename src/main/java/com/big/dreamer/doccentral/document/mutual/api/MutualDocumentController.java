package com.big.dreamer.doccentral.document.mutual.api;

import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.service.MutualDocumentService;
import com.big.dreamer.doccentral.document.mutual.service.MutualRequestValidator;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.model.MutualGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.license.service.LicenseService;
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
@RequestMapping("/api/v1/documents/mutual")
public class MutualDocumentController {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
    private final MutualDocumentService service;
    private final GeneratedDocumentStorage storage;
    private final LicenseService licenseService;
    private final GeneratedDocumentHistoryRepository historyRepository;

    public MutualDocumentController(MutualDocumentService service, GeneratedDocumentStorage storage,
                                    LicenseService licenseService,
                                    GeneratedDocumentHistoryRepository historyRepository) {
        this.service = service;
        this.storage = storage;
        this.licenseService = licenseService;
        this.historyRepository = historyRepository;
    }

    @PostMapping
    public ResponseEntity<byte[]> generate(@Valid @RequestBody MutualDocumentRequest request,
                                           @RequestParam(defaultValue = "docx") String format) {
        licenseService.requireActive();
        MutualRequestValidator.validate(request);
        DocumentFormat documentFormat = DocumentFormat.from(format);
        byte[] contents = documentFormat.isPdf() ? service.createPdfDocument(request) : service.createDocument(request);
        String fileName = "mutuo_" + FILE_DATE.format(Instant.now()) + "." + documentFormat.extension();
        storage.save(fileName, contents);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentFormat.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .body(contents);
    }

    @PostMapping("/history")
    public ResponseEntity<byte[]> generateTracked(
            @Valid @RequestBody MutualGenerationRequest request,
            @RequestParam(defaultValue = "docx") String format) {
        licenseService.requireActive();
        MutualRequestValidator.validate(request.document());
        DocumentFormat documentFormat = DocumentFormat.from(format);
        byte[] contents = documentFormat.isPdf()
                ? service.createPdfDocument(request.document())
                : service.createDocument(request.document());
        Instant createdAt = Instant.now();
        String fileName = "mutuo_" + FILE_DATE.format(createdAt) + "." + documentFormat.extension();
        storage.save(fileName, contents);
        GeneratedDocumentMetadata metadata = historyRepository.saveMutual(
                fileName, createdAt.toString(), request.document(), request.draft());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentFormat.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .header("X-Document-History-Id", metadata.id())
                .body(contents);
    }
}
