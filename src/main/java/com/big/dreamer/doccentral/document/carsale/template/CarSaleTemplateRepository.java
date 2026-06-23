package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CarSaleTemplateRepository {

    private static final Map<String, String> DEFAULT_TEMPLATES = new LinkedHashMap<>();
    private static final Map<String, String> LEGACY_TEMPLATES = new LinkedHashMap<>();

    static {
        DEFAULT_TEMPLATES.put("people-document.txt", CarSaleTemplates.PEOPLE_DOCUMENT);
        DEFAULT_TEMPLATES.put("people-authentic.txt", CarSaleTemplates.PEOPLE_AUTHENTIC);
        DEFAULT_TEMPLATES.put("car-document.txt", CarSaleTemplates.CAR_DOCUMENT);
        DEFAULT_TEMPLATES.put("car-authentic.txt", CarSaleTemplates.CAR_AUTHENTIC);
        DEFAULT_TEMPLATES.put("document.txt", CarSaleTemplates.DOCUMENT);
        DEFAULT_TEMPLATES.put("first-section-end.txt", CarSaleTemplates.FIRST_SECTION_END);
        DEFAULT_TEMPLATES.put("second-section-end.txt", CarSaleTemplates.SECOND_SECTION_END);
        DEFAULT_TEMPLATES.put("legal-authentic.txt", CarSaleTemplates.LEGAL_AUTHENTIC);

        LEGACY_TEMPLATES.put("people-document.txt", CarSaleTemplates.LEGACY_PEOPLE_DOCUMENT);
        LEGACY_TEMPLATES.put("people-authentic.txt", CarSaleTemplates.LEGACY_PEOPLE_AUTHENTIC);
        LEGACY_TEMPLATES.put("document.txt", CarSaleTemplates.LEGACY_DOCUMENT);
    }

    private final ApplicationDirectories directories;

    public CarSaleTemplateRepository(ApplicationDirectories directories) {
        this.directories = directories;
    }

    @PostConstruct
    public void initializeTemplates() {
        Path templatesDirectory = directories.templatesDirectory();
        try {
            Files.createDirectories(templatesDirectory);
            for (Map.Entry<String, String> template : DEFAULT_TEMPLATES.entrySet()) {
                Path templatePath = templatesDirectory.resolve(template.getKey());
                if (Files.notExists(templatePath)) {
                    Files.writeString(templatePath, template.getValue(), StandardCharsets.UTF_8);
                } else if (template.getValue() != null
                        && template.getValue().equals(DEFAULT_TEMPLATES.get(template.getKey()))
                        && LEGACY_TEMPLATES.containsKey(template.getKey())) {
                    migrateLegacyTemplate(templatePath, template.getKey());
                }
            }
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to initialize local templates.", exception);
        }
    }

    private void migrateLegacyTemplate(Path templatePath, String fileName) throws IOException {
        String currentTemplate = Files.readString(templatePath, StandardCharsets.UTF_8);
        if (currentTemplate.equals(LEGACY_TEMPLATES.get(fileName))) {
            Files.writeString(templatePath, DEFAULT_TEMPLATES.get(fileName), StandardCharsets.UTF_8);
        }
    }

    public Templates load() {
        try {
            return new Templates(
                    read("people-document.txt"),
                    read("people-authentic.txt"),
                    read("car-document.txt"),
                    read("car-authentic.txt"),
                    read("document.txt"),
                    read("first-section-end.txt"),
                    read("second-section-end.txt"),
                    read("legal-authentic.txt"));
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to read local templates.", exception);
        }
    }

    private String read(String fileName) throws IOException {
        return Files.readString(directories.templatesDirectory().resolve(fileName), StandardCharsets.UTF_8);
    }

    public record Templates(
            String peopleDocument,
            String peopleAuthentic,
            String carDocument,
            String carAuthentic,
            String document,
            String firstSectionEnd,
            String secondSectionEnd,
            String legalAuthentic) {
    }
}
