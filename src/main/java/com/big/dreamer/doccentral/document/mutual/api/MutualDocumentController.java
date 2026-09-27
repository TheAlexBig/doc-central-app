package com.big.dreamer.doccentral.document.mutual.api;

import com.big.dreamer.doccentral.document.DocumentDownloadResponse;
import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.DocumentGenerationCoordinator;
import com.big.dreamer.doccentral.document.history.model.MutualGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.service.*;
import com.big.dreamer.doccentral.license.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents/mutual")
public class MutualDocumentController {
    private final MutualDocumentService service;
    private final DocumentGenerationCoordinator coordinator;
    private final LicenseService license;
    private final GeneratedDocumentHistoryRepository history;
    private final MutualRulesService rules;

    public MutualDocumentController(MutualDocumentService service,
                                    DocumentGenerationCoordinator coordinator,
                                    LicenseService license,
                                    GeneratedDocumentHistoryRepository history,
                                    MutualRulesService rules) {
        this.service = service;
        this.coordinator = coordinator;
        this.license = license;
        this.history = history;
        this.rules = rules;
    }

    @PostMapping
    public ResponseEntity<byte[]> generate(@Valid @RequestBody MutualDocumentRequest request,
                                           @RequestParam(defaultValue = "docx") String format) {
        license.requireActive();
        MutualRequestValidator.validate(request);
        DocumentFormat selected = DocumentFormat.from(format);
        return DocumentDownloadResponse.from(coordinator.generate(
                "mutuo", "", selected, () -> render(request, selected), null));
    }

    @PostMapping("/history")
    public ResponseEntity<byte[]> generateTracked(
            @Valid @RequestBody MutualGenerationRequest request,
            @RequestParam(defaultValue = "docx") String format) {
        license.requireActive();
        MutualRequestValidator.validate(request.document());
        DocumentFormat selected = DocumentFormat.from(format);
        return DocumentDownloadResponse.from(coordinator.generate(
                "mutuo", "", selected, () -> render(request.document(), selected),
                (fileName, createdAt) -> history.saveMutual(
                        fileName, createdAt, request.document(), request.draft())));
    }

    @PostMapping("/calculate")
    public MutualFinancialPlan calculate(@Valid @RequestBody MutualDocumentRequest request) {
        license.requireActive();
        MutualRequestValidator.validate(request);
        MutualFinancialPlan plan = rules.resolve(request).financialPlan();
        if (plan == null) throw new IllegalArgumentException("Faltan datos estructurados para calcular el mutuo.");
        return plan;
    }

    private byte[] render(MutualDocumentRequest request, DocumentFormat format) {
        return format.isPdf() ? service.createPdfDocument(request) : service.createDocument(request);
    }
}
