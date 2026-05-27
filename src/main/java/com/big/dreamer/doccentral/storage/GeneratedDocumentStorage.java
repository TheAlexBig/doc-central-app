package com.big.dreamer.doccentral.storage;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
}
