package com.big.dreamer.doccentral.document.template;

import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.storage.LocalJsonFileWriter;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Local editable blocks, isolated by document type. */
public class EditableTemplateRepository {
    private static final Pattern PLACEHOLDER = Pattern.compile(":([A-Za-z][A-Za-z0-9]*)");
    private final Path directory;
    private final Map<String, Definition> definitions = new LinkedHashMap<>();
    private final Map<String, List<String>> legacyDefaults;

    protected EditableTemplateRepository(Path directory, List<Definition> definitions) {
        this(directory, definitions, Map.of());
    }

    protected EditableTemplateRepository(Path directory, List<Definition> definitions,
                                         Map<String, List<String>> legacyDefaults) {
        this.directory = directory;
        this.legacyDefaults = Map.copyOf(legacyDefaults);
        definitions.forEach(definition -> this.definitions.put(definition.name(), definition));
    }

    @PostConstruct
    public synchronized void initializeTemplates() {
        try {
            Files.createDirectories(directory);
            for (Definition definition : definitions.values()) {
                Path path = directory.resolve(definition.name());
                if (Files.notExists(path)) {
                    LocalJsonFileWriter.write(path, definition.content());
                } else {
                    String current = Files.readString(path, StandardCharsets.UTF_8);
                    if (legacyDefaults.getOrDefault(definition.name(), List.of()).contains(current)) {
                        LocalJsonFileWriter.write(path, definition.content());
                    }
                }
            }
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudieron inicializar las plantillas.", exception);
        }
    }

    public synchronized Map<String, String> findAll() {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            for (Definition definition : definitions.values()) {
                result.put(definition.name(), Files.readString(directory.resolve(definition.name()), StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudieron leer las plantillas.", exception);
        }
        return result;
    }

    public synchronized List<TemplateView> list() {
        Map<String, String> current = findAll();
        return definitions.values().stream().map(definition -> view(definition, current.get(definition.name()))).toList();
    }

    public synchronized TemplateView save(String name, String content) {
        Definition definition = requireKnown(name);
        if (content == null || content.isBlank()) throw new IllegalArgumentException("La plantilla no puede estar vacía.");
        Set<String> expected = placeholders(definition.content());
        Set<String> actual = placeholders(content);
        Set<String> missing = new LinkedHashSet<>(expected);
        missing.removeAll(actual);
        if (!missing.isEmpty()) throw new IllegalArgumentException("Faltan variables obligatorias: " + String.join(", ", missing));
        Set<String> unknown = new LinkedHashSet<>(actual);
        unknown.removeAll(expected);
        if (!unknown.isEmpty()) throw new IllegalArgumentException("Variables desconocidas: " + String.join(", ", unknown));
        try {
            LocalJsonFileWriter.write(directory.resolve(name), content);
        } catch (IOException exception) {
            throw new DocumentGenerationException("No se pudo guardar la plantilla.", exception);
        }
        return view(definition, content);
    }

    public synchronized TemplateView reset(String name) {
        return save(name, requireKnown(name).content());
    }

    private Definition requireKnown(String name) {
        Definition definition = definitions.get(name);
        if (definition == null) throw new IllegalArgumentException("Plantilla desconocida.");
        return definition;
    }

    private TemplateView view(Definition definition, String content) {
        return new TemplateView(definition.name(), definition.label(), definition.section(), definition.condition(),
                content, definition.content(), List.copyOf(placeholders(definition.content())), content.equals(definition.content()));
    }

    public static Set<String> placeholders(String content) {
        Set<String> result = new LinkedHashSet<>();
        Matcher matcher = PLACEHOLDER.matcher(content);
        while (matcher.find()) result.add(matcher.group());
        return result;
    }

    public static String render(String content, Map<String, String> values) {
        Matcher matcher = PLACEHOLDER.matcher(content);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String value = values.get(matcher.group(1));
            if (value == null) throw new IllegalArgumentException("Variable sin valor: " + matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    public record Definition(String name, String label, String section, String condition, String content) {}
    public record TemplateView(String name, String label, String section, String condition, String content,
                               String defaultContent, List<String> requiredVariables, boolean usingDefault) {}
}
