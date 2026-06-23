package com.big.dreamer.doccentral.storage;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class GeneratedDocumentStorage {

    private final ApplicationDirectories directories;

    public GeneratedDocumentStorage(ApplicationDirectories directories) {
        this.directories = directories;
    }

    public void save(String fileName, byte[] contents) {
        Path documentPath = directories.documentsDirectory().resolve(fileName);
        try {
            Files.write(documentPath, contents);
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to save the Word document locally.", exception);
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
            throw new DocumentGenerationException("Unable to read the Word document locally.", exception);
        }
    }
}
