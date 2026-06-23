package com.big.dreamer.doccentral.document.history.api;

import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.document.history.service.GeneratedDocumentHistoryRepository;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.big.dreamer.doccentral.document.carsale.api.CarSaleDocumentController.WORD_CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/v1/documents/history")
public class DocumentHistoryController {

    private final GeneratedDocumentHistoryRepository historyRepository;
    private final GeneratedDocumentStorage documentStorage;

    public DocumentHistoryController(
            GeneratedDocumentHistoryRepository historyRepository,
            GeneratedDocumentStorage documentStorage) {
        this.historyRepository = historyRepository;
        this.documentStorage = documentStorage;
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
    public ResponseEntity<byte[]> downloadHistoryFile(@PathVariable String id) {
        GeneratedDocumentMetadata metadata = getHistoryItem(id);
        byte[] document = documentStorage.read(metadata.fileName())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Generated document file not found."));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(metadata.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(WORD_CONTENT_TYPE))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(document);
    }
}
