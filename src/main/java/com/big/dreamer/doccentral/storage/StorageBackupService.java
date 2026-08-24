package com.big.dreamer.doccentral.storage;

import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class StorageBackupService {

    private static final Set<String> DATA_FILES = Set.of(
            "agents.json", "people.json", "vehicles.json",
            "vehicle-option-exclusions.json", "generated-documents.json");
    private static final long MAX_UNCOMPRESSED_BYTES = 500L * 1024 * 1024;

    private final ApplicationDirectories directories;
    private final ObjectMapper objectMapper;

    public StorageBackupService(ApplicationDirectories directories, ObjectMapper objectMapper) {
        this.directories = directories;
        this.objectMapper = objectMapper;
    }

    public synchronized byte[] exportBackup() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            addText(zip, "manifest.json", objectMapper.writeValueAsString(Map.of(
                    "format", "central-docs-backup", "formatVersion", 1)));
            for (String fileName : DATA_FILES) {
                addFile(zip, directories.dataDirectory().resolve(fileName), "data/" + fileName);
            }
            addDirectory(zip, directories.templatesDirectory(), "templates/");
            addDirectory(zip, directories.documentsDirectory(), "documents/");
            zip.finish();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new StorageBackupException("No se pudo crear el respaldo.", exception);
        }
    }

    public synchronized void restoreBackup(byte[] archive) {
        try {
            Map<String, byte[]> entries = readEntries(archive);
            validateManifest(entries.remove("manifest.json"));
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                restoreEntry(entry.getKey(), entry.getValue());
            }
        } catch (IOException exception) {
            throw new StorageBackupException("No se pudo restaurar el respaldo.", exception);
        }
    }

    private Map<String, byte[]> readEntries(byte[] archive) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        long total = 0;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName().replace('\\', '/');
                if (name.startsWith("/") || name.contains("../")) {
                    throw new StorageBackupException("El respaldo contiene una ruta no válida.");
                }
                byte[] contents = zip.readAllBytes();
                total += contents.length;
                if (total > MAX_UNCOMPRESSED_BYTES) {
                    throw new StorageBackupException("El respaldo excede el tamaño permitido.");
                }
                entries.put(name, contents);
            }
        }
        return entries;
    }

    private void validateManifest(byte[] manifest) throws IOException {
        if (manifest == null) throw new StorageBackupException("El archivo no es un respaldo de Central Docs.");
        Map<?, ?> values = objectMapper.readValue(manifest, Map.class);
        if (!"central-docs-backup".equals(values.get("format"))) {
            throw new StorageBackupException("El formato del respaldo no es compatible.");
        }
    }

    private void restoreEntry(String name, byte[] contents) throws IOException {
        if (name.startsWith("data/")) {
            String fileName = name.substring("data/".length());
            if (!DATA_FILES.contains(fileName)) return;
            objectMapper.readTree(contents);
            LocalJsonFileWriter.write(directories.dataDirectory().resolve(fileName),
                    new String(contents, StandardCharsets.UTF_8));
        } else if (name.startsWith("templates/")) {
            String fileName = name.substring("templates/".length());
            if (!fileName.matches("[a-z-]+\\.txt")) return;
            LocalJsonFileWriter.write(directories.templatesDirectory().resolve(fileName),
                    new String(contents, StandardCharsets.UTF_8));
        } else if (name.startsWith("documents/")) {
            String relative = name.substring("documents/".length());
            if (relative.isBlank() || relative.equalsIgnoreCase("license.json")) return;
            Path target = directories.documentsDirectory().resolve(relative).normalize();
            if (!target.startsWith(directories.documentsDirectory())) return;
            Files.createDirectories(target.getParent());
            Files.write(target, contents);
        }
    }

    private void addDirectory(ZipOutputStream zip, Path directory, String prefix) throws IOException {
        if (Files.notExists(directory)) return;
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String relative = directory.relativize(path).toString().replace('\\', '/');
                if (relative.equalsIgnoreCase("license.json") || relative.startsWith("backups/")) continue;
                addFile(zip, path, prefix + relative);
            }
        }
    }

    private void addFile(ZipOutputStream zip, Path file, String name) throws IOException {
        if (Files.exists(file) && Files.isRegularFile(file)) {
            zip.putNextEntry(new ZipEntry(name));
            Files.copy(file, zip);
            zip.closeEntry();
        }
    }

    private void addText(ZipOutputStream zip, String name, String text) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(text.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
