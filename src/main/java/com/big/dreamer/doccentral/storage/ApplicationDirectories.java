package com.big.dreamer.doccentral.storage;

import com.big.dreamer.doccentral.desktop.UserDataLocations;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class ApplicationDirectories {

    private final Path dataDirectory;
    private final Path documentsDirectory;

    public ApplicationDirectories(
            @Value("${app.storage.data-directory:}") String configuredDataDirectory,
            @Value("${app.storage.documents-directory:}") String configuredDocumentsDirectory) {
        this.dataDirectory = resolve(configuredDataDirectory, UserDataLocations.applicationDataDirectory());
        this.documentsDirectory = resolve(configuredDocumentsDirectory, UserDataLocations.documentsDirectory());
    }

    @PostConstruct
    public void initialize() {
        try {
            Files.createDirectories(dataDirectory);
            Files.createDirectories(documentsDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create Central Docs local directories.", exception);
        }
    }

    public Path templatesDirectory() {
        return dataDirectory.resolve("templates").resolve("car-sale");
    }

    public Path documentsDirectory() {
        return documentsDirectory;
    }

    public Path agentsFile() {
        return dataDirectory.resolve("agents.json");
    }

    public Path peopleFile() {
        return dataDirectory.resolve("people.json");
    }

    public Path vehiclesFile() {
        return dataDirectory.resolve("vehicles.json");
    }

    public Path vehicleOptionExclusionsFile() {
        return dataDirectory.resolve("vehicle-option-exclusions.json");
    }

    public Path generatedDocumentsHistoryFile() {
        return dataDirectory.resolve("generated-documents.json");
    }

    private Path resolve(String configuredDirectory, Path defaultDirectory) {
        return configuredDirectory == null || configuredDirectory.isBlank()
                ? defaultDirectory
                : Path.of(configuredDirectory);
    }
}
