package com.big.dreamer.doccentral.storage;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/backup")
public class StorageBackupController {

    private final StorageBackupService service;

    public StorageBackupController(StorageBackupService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<byte[]> download() {
        String fileName = "central-docs-backup-" + LocalDate.now() + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(fileName).build().toString())
                .body(service.exportBackup());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Boolean> restore(@RequestPart("file") MultipartFile file) throws IOException {
        service.restoreBackup(file.getBytes());
        return Map.of("restored", true);
    }
}
