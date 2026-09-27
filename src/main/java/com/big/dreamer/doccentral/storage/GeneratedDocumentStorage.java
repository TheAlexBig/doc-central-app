package com.big.dreamer.doccentral.storage;

import com.big.dreamer.doccentral.document.DocumentGenerationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Service
public class GeneratedDocumentStorage {

    private final ApplicationDirectories directories;

    public GeneratedDocumentStorage(ApplicationDirectories directories) {
        this.directories = directories;
    }

    public void save(String fileName, byte[] contents) {
        Path documentPath = directories.documentsDirectory().resolve(fileName);
        Path temporaryPath = directories.documentsDirectory().resolve(fileName + "." + UUID.randomUUID() + ".tmp");
        try {
            Files.write(temporaryPath, contents);
            try {
                Files.move(temporaryPath, documentPath, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (java.nio.file.AtomicMoveNotSupportedException exception) {
                Files.move(temporaryPath, documentPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo guardar el documento generado.", exception);
        } finally {
            try {
                Files.deleteIfExists(temporaryPath);
            } catch (IOException ignored) {
                // The final document has already been committed or the original failure is more useful.
            }
        }
    }

    public void delete(String fileName) {
        Path documentPath = directories.documentsDirectory().resolve(fileName).normalize();
        if (!documentPath.startsWith(directories.documentsDirectory())) return;
        try {
            Files.deleteIfExists(documentPath);
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo eliminar el documento generado.", exception);
        }
    }

    public Optional<byte[]> read(String fileName) {
        Path documentPath = directories.documentsDirectory().resolve(fileName).normalize();
        if (!documentPath.startsWith(directories.documentsDirectory()) || Files.notExists(documentPath)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.readAllBytes(documentPath));
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo leer el documento generado.", exception);
        }
    }
}
