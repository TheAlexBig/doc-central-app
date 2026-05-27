package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleDocumentService;
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

    public CarSaleDocumentController(
            CarSaleDocumentService documentService,
            GeneratedDocumentStorage documentStorage) {
        this.documentService = documentService;
        this.documentStorage = documentStorage;
    }

    @PostMapping(value = "/car-sale", produces = WORD_CONTENT_TYPE)
    public ResponseEntity<byte[]> generateCarSaleDocument(
            @Valid @RequestBody CarSaleDocumentRequest request) {
        byte[] document = documentService.createDocument(request);
        String fileName = "compra-venta_" + FILE_CREATED_AT.format(Instant.now()) + ".docx";
        documentStorage.save(fileName, document);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(WORD_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(document);
    }
}
