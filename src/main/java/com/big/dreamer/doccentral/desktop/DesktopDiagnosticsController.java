package com.big.dreamer.doccentral.desktop;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping("/api/v1/desktop/diagnostics")
public class DesktopDiagnosticsController {

    private static final String APPLICATION_LOG_FILE_NAME = "central-docs.log";
    private static final String STARTUP_FAILURE_LOG_FILE_NAME = "startup-failure.log";

    private final ApplicationDirectories directories;

    public DesktopDiagnosticsController(ApplicationDirectories directories) {
        this.directories = directories;
    }

    @GetMapping
    public DesktopDiagnostics diagnostics() {
        Path logsDirectory = logsDirectory();
        return new DesktopDiagnostics(
                directories.documentsDirectory().toString(),
                logsDirectory.toString(),
                logsDirectory.resolve(APPLICATION_LOG_FILE_NAME).toString(),
                logsDirectory.resolve(STARTUP_FAILURE_LOG_FILE_NAME).toString());
    }

    @PostMapping("/logs-folder")
    public ResponseEntity<Map<String, Boolean>> openLogsFolder() {
        try {
            Path logsDirectory = logsDirectory();
            Files.createDirectories(logsDirectory);
            open(logsDirectory);
            return ResponseEntity.ok(Map.of("opened", true));
        } catch (IOException exception) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to open the logs folder.", exception);
        }
    }

    private Path logsDirectory() {
        return UserDataLocations.applicationDataDirectory().resolve("logs");
    }

    private void open(Path directory) throws IOException {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(directory.toFile());
            return;
        }
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            new ProcessBuilder("explorer.exe", directory.toString()).start();
            return;
        }
        throw new IOException("No folder launcher is available.");
    }

    public record DesktopDiagnostics(
            String documentsDirectory,
            String logsDirectory,
            String applicationLog,
            String startupFailureLog) {
    }
}
