package com.big.dreamer.doccentral.document.marriage.api;

import com.big.dreamer.doccentral.document.DocumentDownloadResponse;
import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.DocumentGenerationCoordinator;
import com.big.dreamer.doccentral.document.history.model.MarriageGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.big.dreamer.doccentral.document.marriage.service.*;
import com.big.dreamer.doccentral.license.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/marriage")
public class MarriageDocumentController {
    private final MarriageDocumentService service;
    private final MarriageRulesService rules;
    private final DocumentGenerationCoordinator coordinator;
    private final GeneratedDocumentHistoryRepository history;
    private final LicenseService license;

    public MarriageDocumentController(MarriageDocumentService service, MarriageRulesService rules,
                                      DocumentGenerationCoordinator coordinator,
                                      GeneratedDocumentHistoryRepository history,
                                      LicenseService license) {
        this.service = service;
        this.rules = rules;
        this.coordinator = coordinator;
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
        DocumentFormat selected = DocumentFormat.from(format);
        return DocumentDownloadResponse.from(coordinator.generate(
                "matrimonio", "", selected, () -> render(request.document(), selected),
                (fileName, createdAt) -> history.saveMarriage(
                        fileName, createdAt, request.document(), request.draft())));
    }

    private byte[] render(MarriageDocumentRequest request, DocumentFormat format) {
        return format.isPdf() ? service.createPdfDocument(request) : service.createDocument(request);
    }
}
