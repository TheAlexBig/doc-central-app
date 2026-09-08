package com.big.dreamer.doccentral.document.marriage.api;

import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.model.MarriageGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.big.dreamer.doccentral.document.marriage.service.MarriageDocumentService;
import com.big.dreamer.doccentral.document.marriage.service.MarriageRuleResolution;
import com.big.dreamer.doccentral.document.marriage.service.MarriageRulesService;
import com.big.dreamer.doccentral.license.service.LicenseService;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/documents/marriage")
public class MarriageDocumentController {
    private static final DateTimeFormatter FILE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
    private final MarriageDocumentService service;
    private final MarriageRulesService rules;
    private final GeneratedDocumentStorage storage;
    private final GeneratedDocumentHistoryRepository history;
    private final LicenseService license;

    public MarriageDocumentController(MarriageDocumentService service, MarriageRulesService rules,
                                      GeneratedDocumentStorage storage,
                                      GeneratedDocumentHistoryRepository history,
                                      LicenseService license) {
        this.service = service;
        this.rules = rules;
        this.storage = storage;
        this.history = history;
        this.license = license;
    }

    @PostMapping("/requirements")
    public MarriageRuleResolution requirements(@Valid @RequestBody MarriageDocumentRequest request) {
        license.requireActive();
        return rules.resolve(request);
    }

    @PostMapping("/history")
    public ResponseEntity<byte[]> generateTracked(@Valid @RequestBody MarriageGenerationRequest request,
                                                  @RequestParam(defaultValue = "docx") String format) {
        license.requireActive();
        rules.validateForGeneration(request.document());
        DocumentFormat documentFormat = DocumentFormat.from(format);
        byte[] contents = documentFormat.isPdf()
                ? service.createPdfDocument(request.document())
                : service.createDocument(request.document());
        Instant createdAt = Instant.now();
        String fileName = "matrimonio_" + FILE_DATE.format(createdAt) + "." + documentFormat.extension();
        storage.save(fileName, contents);
        GeneratedDocumentMetadata metadata = history.saveMarriage(
                fileName, createdAt.toString(), request.document(), request.draft());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(documentFormat.contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName, StandardCharsets.UTF_8).build().toString())
                .header("X-Document-History-Id", metadata.id())
                .body(contents);
    }
}
