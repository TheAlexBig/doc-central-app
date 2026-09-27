package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.DocumentDownloadResponse;
import com.big.dreamer.doccentral.document.DocumentFormat;
import com.big.dreamer.doccentral.document.DocumentGenerationCoordinator;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidator;
import com.big.dreamer.doccentral.document.history.model.CarSaleGenerationRequest;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.license.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
public class CarSaleDocumentController {
    public static final String WORD_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String PDF_CONTENT_TYPE = "application/pdf";
    public static final String WORD_FORMAT = "docx";
    public static final String PDF_FORMAT = "pdf";

    private final CarSaleDocumentService service;
    private final DocumentGenerationCoordinator coordinator;
    private final GeneratedDocumentHistoryRepository history;
    private final LicenseService license;

    public CarSaleDocumentController(CarSaleDocumentService service,
                                     DocumentGenerationCoordinator coordinator,
                                     GeneratedDocumentHistoryRepository history,
                                     LicenseService license) {
        this.service = service;
        this.coordinator = coordinator;
        this.history = history;
        this.license = license;
    }

    @PostMapping("/car-sale")
    public ResponseEntity<byte[]> generateCarSaleDocument(
            @Valid @RequestBody CarSaleDocumentRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        license.requireActive();
        CarSaleRequestValidator.validate(request);
        DocumentFormat selected = DocumentFormat.from(format);
        return DocumentDownloadResponse.from(coordinator.generate(
                "compra-venta", request.vehicle().licensePlate(), selected,
                () -> render(request, selected), null));
    }

    @PostMapping("/car-sale/history")
    public ResponseEntity<byte[]> generateTrackedCarSaleDocument(
            @Valid @RequestBody CarSaleGenerationRequest request,
            @RequestParam(defaultValue = WORD_FORMAT) String format) {
        license.requireActive();
        CarSaleRequestValidator.validate(request.document());
        DocumentFormat selected = DocumentFormat.from(format);
        return DocumentDownloadResponse.from(coordinator.generate(
                "compra-venta", draftLicensePlate(request), selected,
                () -> render(request.document(), selected),
                (fileName, createdAt) -> history.saveCarSale(
                        fileName, createdAt, request.document(), request.draft())));
    }

    private byte[] render(CarSaleDocumentRequest request, DocumentFormat format) {
        return format.isPdf() ? service.createPdfDocument(request) : service.createDocument(request);
    }

    private String draftLicensePlate(CarSaleGenerationRequest request) {
        Object vehicle = request.draft() == null ? null : request.draft().get("carStates");
        if (vehicle instanceof Map<?, ?> values && values.get("placa") != null
                && !values.get("placa").toString().isBlank()) {
            return values.get("placa").toString();
        }
        return request.document().vehicle().licensePlate();
    }
}
