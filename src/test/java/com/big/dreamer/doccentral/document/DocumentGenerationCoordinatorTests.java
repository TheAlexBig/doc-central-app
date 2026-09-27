package com.big.dreamer.doccentral.document;

import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.storage.GeneratedDocumentStorage;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DocumentGenerationCoordinatorTests {

    private final GeneratedDocumentStorage storage = mock(GeneratedDocumentStorage.class);
    private final DocumentGenerationCoordinator coordinator = new DocumentGenerationCoordinator(storage);

    @Test
    void savesAndRegistersOneGeneratedArtifact() {
        byte[] contents = {1, 2, 3};
        AtomicReference<String> registeredFile = new AtomicReference<>();

        DocumentGenerationCoordinator.GeneratedArtifact artifact = coordinator.generate(
                "mutuo", "DUI 01234567-8", DocumentFormat.PDF, () -> contents,
                (fileName, createdAt) -> {
                    registeredFile.set(fileName);
                    assertThat(Instant.parse(createdAt)).isNotNull();
                    return metadata("history-id", fileName, createdAt);
                });

        assertThat(artifact.fileName())
                .startsWith("mutuo_DUI-01234567-8_")
                .endsWith(".pdf")
                .isEqualTo(registeredFile.get());
        assertThat(artifact.contents()).isSameAs(contents);
        assertThat(artifact.historyId()).isEqualTo("history-id");
        verify(storage).save(artifact.fileName(), contents);
    }

    @Test
    void deletesTheFileWhenHistoryRegistrationFails() {
        RuntimeException historyFailure = new RuntimeException("history failed");
        AtomicReference<String> generatedFile = new AtomicReference<>();

        assertThatThrownBy(() -> coordinator.generate(
                "matrimonio", "Ana y Luis", DocumentFormat.WORD, () -> new byte[]{4},
                (fileName, createdAt) -> {
                    generatedFile.set(fileName);
                    throw historyFailure;
                }))
                .isSameAs(historyFailure);

        verify(storage).delete(generatedFile.get());
    }

    private GeneratedDocumentMetadata metadata(String id, String fileName, String createdAt) {
        return new GeneratedDocumentMetadata(id, 2, "mutual", fileName, createdAt,
                "Mutuo", "", "", "", null, Map.of(), null, null);
    }
}
