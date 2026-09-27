package com.big.dreamer.doccentral.document;

import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Service
public class DocumentGenerationCoordinator {
    private static final DateTimeFormatter FILE_DATE =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS").withZone(ZoneOffset.UTC);
    private final GeneratedDocumentStorage storage;

    public DocumentGenerationCoordinator(GeneratedDocumentStorage storage) {
        this.storage = storage;
    }

    public GeneratedArtifact generate(String prefix, String identity, DocumentFormat format,
                                      Supplier<byte[]> renderer, HistoryRegistration history) {
        Instant createdAt = Instant.now();
        String fileName = prefix + "_" + safe(identity) + FILE_DATE.format(createdAt)
                + "." + format.extension();
        byte[] contents = renderer.get();
        storage.save(fileName, contents);
        try {
            GeneratedDocumentMetadata metadata = history == null
                    ? null
                    : history.save(fileName, createdAt.toString());
            return new GeneratedArtifact(fileName, contents, format, metadata == null ? null : metadata.id());
        } catch (RuntimeException exception) {
            try {
                storage.delete(fileName);
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    private String safe(String identity) {
        if (identity == null || identity.isBlank()) return "";
        String safe = identity.trim().replaceAll("[^\\p{L}\\p{N}-]+", "-");
        return safe.isBlank() ? "" : safe + "_";
    }

    @FunctionalInterface
    public interface HistoryRegistration {
        GeneratedDocumentMetadata save(String fileName, String createdAt);
    }

    public record GeneratedArtifact(String fileName, byte[] contents, DocumentFormat format, String historyId) {
    }
}
