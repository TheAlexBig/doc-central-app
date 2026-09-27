package com.big.dreamer.doccentral.document.template;

import com.big.dreamer.doccentral.document.DocumentGenerationException;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Loads the shipped template definition for one document type. */
public final class TemplateCatalog {
    private static final String ROOT = "document-templates/";

    private TemplateCatalog() {
    }

    public static Catalog load(String documentType) {
        String directory = ROOT + documentType + "/";
        try (InputStream manifestStream = resource(directory + "manifest.json")) {
            Manifest manifest = JsonMapper.builder().build().readValue(manifestStream, Manifest.class);
            Set<String> names = new HashSet<>();
            List<EditableTemplateRepository.Definition> definitions = manifest.templates().stream()
                    .map(entry -> {
                        if (!names.add(entry.name())) {
                            throw new IllegalArgumentException("Plantilla duplicada: " + entry.name());
                        }
                        return new EditableTemplateRepository.Definition(
                                entry.name(), entry.label(), entry.section(), entry.condition(),
                                readText(directory + entry.name()));
                    })
                    .toList();
            if (definitions.isEmpty()) {
                throw new IllegalArgumentException("El catálogo no contiene plantillas.");
            }
            return new Catalog(documentType, manifest.version(), definitions);
        } catch (IOException | IllegalArgumentException exception) {
            throw new DocumentGenerationException(
                    "No se pudo cargar el catálogo de plantillas para " + documentType + ".", exception);
        }
    }

    private static String readText(String path) {
        try (InputStream stream = resource(path)) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).stripTrailing();
        } catch (IOException exception) {
            throw new IllegalArgumentException("No se pudo leer " + path + ".", exception);
        }
    }

    private static InputStream resource(String path) {
        InputStream stream = TemplateCatalog.class.getClassLoader().getResourceAsStream(path);
        if (stream == null) throw new IllegalArgumentException("Recurso inexistente: " + path);
        return stream;
    }

    public record Catalog(String documentType, int version,
                          List<EditableTemplateRepository.Definition> definitions) {
    }

    private record Manifest(int version, List<Entry> templates) {
    }

    private record Entry(String name, String label, String section, String condition) {
    }
}
