package com.big.dreamer.doccentral.document.history.service;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.history.model.GeneratedDocumentMetadata;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class GeneratedDocumentHistoryRepository {

    private static final String CAR_SALE_TYPE = "car-sale";
    private final Path historyFile;
    private final ObjectMapper objectMapper;

    public GeneratedDocumentHistoryRepository(ApplicationDirectories directories, ObjectMapper objectMapper) {
        this.historyFile = directories.generatedDocumentsHistoryFile();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void initialize() {
        if (Files.notExists(historyFile)) {
            write(List.of());
        }
    }

    public synchronized List<GeneratedDocumentMetadata> findAll() {
        try {
            GeneratedDocumentMetadata[] documents = objectMapper.readValue(
                    Files.readString(historyFile, StandardCharsets.UTF_8),
                    GeneratedDocumentMetadata[].class);
            return sortByMostRecent(List.of(documents));
        } catch (IOException exception) {
            throw new DocumentHistoryStorageException("Unable to read generated document history.", exception);
        }
    }

    public synchronized Optional<GeneratedDocumentMetadata> findById(String id) {
        return findAll().stream()
                .filter(document -> document.id().equals(id))
                .findFirst();
    }

    public synchronized GeneratedDocumentMetadata saveCarSale(
            String fileName,
            String createdAt,
            CarSaleDocumentRequest document,
            Map<String, Object> draft) {
        GeneratedDocumentMetadata metadata = new GeneratedDocumentMetadata(
                UUID.randomUUID().toString(),
                CAR_SALE_TYPE,
                fileName,
                createdAt,
                title(document),
                fullName(document.buyer().givenName(), document.buyer().lastName()),
                fullName(document.seller().givenName(), document.seller().lastName()),
                vehicleName(document),
                document,
                draft == null ? Map.of() : draft);

        List<GeneratedDocumentMetadata> documents = new ArrayList<>(findAll());
        documents.add(0, metadata);
        write(sortByMostRecent(documents));
        return metadata;
    }

    private List<GeneratedDocumentMetadata> sortByMostRecent(List<GeneratedDocumentMetadata> documents) {
        return documents.stream()
                .sorted(Comparator.comparing(
                        GeneratedDocumentMetadata::createdAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String title(CarSaleDocumentRequest document) {
        return "Compra venta - " + vehicleName(document);
    }

    private String vehicleName(CarSaleDocumentRequest document) {
        return String.join(" ",
                document.vehicle().brand(),
                document.vehicle().model(),
                document.vehicle().licensePlate()).trim();
    }

    private String fullName(String givenName, String lastName) {
        return String.join(" ", givenName, lastName).trim();
    }

    private void write(List<GeneratedDocumentMetadata> documents) {
        Path temporaryFile = historyFile.resolveSibling(historyFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(historyFile.getParent());
            Files.writeString(temporaryFile, objectMapper.writeValueAsString(documents), StandardCharsets.UTF_8);
            Files.move(temporaryFile, historyFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new DocumentHistoryStorageException("Unable to save generated document history.", exception);
        }
    }
}
