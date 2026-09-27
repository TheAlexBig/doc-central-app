package com.big.dreamer.doccentral.document;

import com.big.dreamer.doccentral.document.DocumentGenerationCoordinator.GeneratedArtifact;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

public final class DocumentDownloadResponse {
    private DocumentDownloadResponse() {
    }

    public static ResponseEntity<byte[]> from(GeneratedArtifact artifact) {
        var response = ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.format().contentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(artifact.fileName(), StandardCharsets.UTF_8).build().toString());
        if (artifact.historyId() != null) response.header("X-Document-History-Id", artifact.historyId());
        return response.body(artifact.contents());
    }
}
