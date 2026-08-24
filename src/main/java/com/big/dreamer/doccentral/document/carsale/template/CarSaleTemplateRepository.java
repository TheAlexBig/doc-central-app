package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.storage.LocalJsonFileWriter;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;
import java.util.regex.Pattern;

@Component
public class CarSaleTemplateRepository {

    private static final Pattern PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9]*)");

    private static final Map<String, String> DEFAULT_TEMPLATES = new LinkedHashMap<>();
    private static final Map<String, List<String>> LEGACY_TEMPLATES = new LinkedHashMap<>();

    static {
        DEFAULT_TEMPLATES.put("people-document.txt", CarSaleTemplates.PEOPLE_DOCUMENT);
        DEFAULT_TEMPLATES.put("people-authentic.txt", CarSaleTemplates.PEOPLE_AUTHENTIC);
        DEFAULT_TEMPLATES.put("car-document.txt", CarSaleTemplates.CAR_DOCUMENT);
        DEFAULT_TEMPLATES.put("car-authentic.txt", CarSaleTemplates.CAR_AUTHENTIC);
        DEFAULT_TEMPLATES.put("document.txt", CarSaleTemplates.DOCUMENT);
        DEFAULT_TEMPLATES.put("document-authentic.txt", CarSaleTemplates.DOCUMENT_AUTHENTIC);
        DEFAULT_TEMPLATES.put("first-section-end.txt", CarSaleTemplates.FIRST_SECTION_END);
        DEFAULT_TEMPLATES.put("second-section-end.txt", CarSaleTemplates.SECOND_SECTION_END);
        DEFAULT_TEMPLATES.put("legal-authentic.txt", CarSaleTemplates.LEGAL_AUTHENTIC);

        LEGACY_TEMPLATES.put("people-document.txt", List.of(
                CarSaleTemplates.LEGACY_PEOPLE_DOCUMENT,
                CarSaleTemplates.PREVIOUS_PEOPLE_DOCUMENT));
        LEGACY_TEMPLATES.put("people-authentic.txt", List.of(
                CarSaleTemplates.LEGACY_PEOPLE_AUTHENTIC,
                CarSaleTemplates.PREVIOUS_PEOPLE_AUTHENTIC));
        LEGACY_TEMPLATES.put("car-document.txt", List.of(
                CarSaleTemplates.RELEASED_CAR_DOCUMENT,
                CarSaleTemplates.PREVIOUS_CAR_DOCUMENT,
                CarSaleTemplates.PREVIOUS_CURRENT_CAR_DOCUMENT));
        LEGACY_TEMPLATES.put("car-authentic.txt", List.of(
                CarSaleTemplates.RELEASED_CAR_AUTHENTIC,
                CarSaleTemplates.PREVIOUS_CAR_AUTHENTIC,
                CarSaleTemplates.PREVIOUS_CURRENT_CAR_AUTHENTIC));
        LEGACY_TEMPLATES.put("document.txt", List.of(
                CarSaleTemplates.LEGACY_DOCUMENT,
                CarSaleTemplates.PREVIOUS_DOCUMENT,
                CarSaleTemplates.PREVIOUS_CURRENT_DOCUMENT,
                CarSaleTemplates.PREVIOUS_DOCUMENT_WITHOUT_DEADLINE));
        LEGACY_TEMPLATES.put("first-section-end.txt", List.of(
                CarSaleTemplates.PREVIOUS_FIRST_SECTION_END,
                CarSaleTemplates.RELEASED_FIRST_SECTION_END));
        LEGACY_TEMPLATES.put("second-section-end.txt", List.of(CarSaleTemplates.PREVIOUS_SECOND_SECTION_END));
        LEGACY_TEMPLATES.put("legal-authentic.txt", List.of(CarSaleTemplates.RELEASED_LEGAL_AUTHENTIC));
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
        String previousDefault = DEFAULT_TEMPLATES.get(fileName).replace(":heavyTruckDetails", "");
        if (LEGACY_TEMPLATES.get(fileName).contains(currentTemplate)
                || previousDefault.equals(currentTemplate)) {
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
                    read("document-authentic.txt"),
                    read("first-section-end.txt"),
                    read("second-section-end.txt"),
                    read("legal-authentic.txt"));
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to read local templates.", exception);
        }
    }

    public synchronized Map<String, String> findAll() {
        LinkedHashMap<String, String> templates = new LinkedHashMap<>();
        DEFAULT_TEMPLATES.keySet().forEach(name -> {
            try {
                templates.put(name, read(name));
            } catch (IOException exception) {
                throw new DocumentGenerationException("Unable to read local templates.", exception);
            }
        });
        return templates;
    }

    public synchronized String save(String fileName, String content) {
        requireKnown(fileName);
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("La plantilla no puede estar vacía.");
        }
        Set<String> missing = new LinkedHashSet<>(placeholders(DEFAULT_TEMPLATES.get(fileName)));
        missing.removeAll(placeholders(content));
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Faltan variables obligatorias: " + String.join(", ", missing));
        }
        try {
            LocalJsonFileWriter.write(directories.templatesDirectory().resolve(fileName), content);
            return content;
        } catch (IOException exception) {
            throw new DocumentGenerationException("Unable to save local template.", exception);
        }
    }

    public synchronized String reset(String fileName) {
        requireKnown(fileName);
        return save(fileName, DEFAULT_TEMPLATES.get(fileName));
    }

    public Map<String, String> defaults() {
        return Map.copyOf(DEFAULT_TEMPLATES);
    }

    public Set<String> placeholders(String content) {
        LinkedHashSet<String> values = new LinkedHashSet<>();
        var matcher = PLACEHOLDER.matcher(content == null ? "" : content);
        while (matcher.find()) values.add(":" + matcher.group(1));
        return values;
    }

    private void requireKnown(String fileName) {
        if (!DEFAULT_TEMPLATES.containsKey(fileName)) {
            throw new IllegalArgumentException("Plantilla desconocida.");
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
            String documentAuthentic,
            String firstSectionEnd,
            String secondSectionEnd,
            String legalAuthentic) {
    }
}
