package com.big.dreamer.doccentral.desktop;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class SupportPackageService {

    private static final int MAX_LOG_BYTES = 1024 * 1024;
    private final Path logsDirectory;
    private final ObjectProvider<BuildProperties> buildProperties;

    public SupportPackageService(
            @Value("${app.support.logs-directory:}") String configuredLogsDirectory,
            ObjectProvider<BuildProperties> buildProperties) {
        this.logsDirectory = configuredLogsDirectory == null || configuredLogsDirectory.isBlank()
                ? UserDataLocations.applicationDataDirectory().resolve("logs")
                : Path.of(configuredLogsDirectory);
        this.buildProperties = buildProperties;
    }

    public byte[] create() {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             ZipOutputStream zip = new ZipOutputStream(bytes, StandardCharsets.UTF_8)) {
            add(zip, "diagnostics.txt", diagnostics());
            addSanitizedLog(zip, "central-docs.log");
            addSanitizedLog(zip, "startup-failure.log");
            zip.finish();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("No se pudo crear el paquete de soporte.", exception);
        }
    }

    private String diagnostics() {
        BuildProperties build = buildProperties.getIfAvailable();
        return "Central Docs support diagnostics\n"
                + "Generated: " + Instant.now() + "\n"
                + "Version: " + (build == null ? "development" : build.getVersion()) + "\n"
                + "Operating system: " + System.getProperty("os.name", "unknown") + "\n"
                + "OS version: " + System.getProperty("os.version", "unknown") + "\n"
                + "Architecture: " + System.getProperty("os.arch", "unknown") + "\n"
                + "Java version: " + System.getProperty("java.version", "unknown") + "\n";
    }

    private void addSanitizedLog(ZipOutputStream zip, String fileName) throws IOException {
        Path file = logsDirectory.resolve(fileName);
        if (Files.notExists(file) || !Files.isRegularFile(file)) return;
        byte[] all = Files.readAllBytes(file);
        int start = Math.max(0, all.length - MAX_LOG_BYTES);
        String log = new String(all, start, all.length - start, StandardCharsets.UTF_8);
        add(zip, fileName, sanitize(log));
    }

    String sanitize(String value) {
        String sanitized = value;
        String home = System.getProperty("user.home", "");
        if (!home.isBlank()) sanitized = sanitized.replace(home, "<USER_HOME>");
        String username = System.getProperty("user.name", "");
        if (!username.isBlank()) sanitized = sanitized.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(username) + "\\b", "<USER>");
        return sanitized
                .replaceAll("(?i)CD-[A-F0-9]{8,24}", "CD-<REDACTED>")
                .replaceAll("\\b\\d{8}-?\\d\\b", "<DUI_REDACTED>")
                .replaceAll("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", "<EMAIL_REDACTED>")
                .replaceAll("(?i)(\"signature\"\\s*:\\s*\")[^\"]+(\")", "$1<REDACTED>$2");
    }

    private void add(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }
}
