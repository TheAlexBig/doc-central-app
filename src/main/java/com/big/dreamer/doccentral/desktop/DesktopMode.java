package com.big.dreamer.doccentral.desktop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class DesktopMode {

    public static final int PORT = 17831;
    public static final URI APPLICATION_URI = URI.create("http://127.0.0.1:" + PORT + "/");
    private static final URI STATUS_URI = URI.create("http://127.0.0.1:" + PORT + "/api/v1/desktop/status");
    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopMode.class);
    private static FileChannel lockChannel;
    private static FileLock applicationLock;

    private DesktopMode() {
    }

    public static boolean isRequested(String[] arguments) {
        return Arrays.stream(arguments).anyMatch(argument ->
                argument.equals("--app.desktop.enabled=true")
                        || activeProfileIncludesDesktop(argument));
    }

    private static boolean activeProfileIncludesDesktop(String argument) {
        String prefix = "--spring.profiles.active=";
        if (!argument.startsWith(prefix)) {
            return false;
        }
        return Arrays.stream(argument.substring(prefix.length()).split(","))
                .anyMatch("desktop"::equals);
    }

    public static boolean reuseExistingInstance() {
        HttpRequest request = HttpRequest.newBuilder(STATUS_URI)
                .timeout(Duration.ofSeconds(1))
                .GET()
                .build();
        try {
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && "central-docs".equals(response.body())) {
                openBrowser();
                return true;
            }
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        return false;
    }

    public static boolean claimNewInstance() {
        Path lockPath = UserDataLocations.applicationDataDirectory().resolve("central-docs.lock");
        try {
            Files.createDirectories(lockPath.getParent());
            lockChannel = FileChannel.open(lockPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            applicationLock = lockChannel.tryLock();
            return applicationLock != null;
        } catch (IOException | OverlappingFileLockException exception) {
            return false;
        }
    }

    public static void awaitExistingInstance() {
        for (int attempt = 0; attempt < 20; attempt++) {
            if (reuseExistingInstance()) {
                return;
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        LOGGER.warn("Another Central Docs process is starting, but its browser interface is not ready yet.");
    }

    public static String[] desktopArguments(String[] arguments) {
        Path configDirectory = UserDataLocations.applicationDataDirectory().resolve("config");
        try {
            Files.createDirectories(configDirectory);
            Path localConfiguration = configDirectory.resolve("application.yml");
            if (Files.notExists(localConfiguration)) {
                Files.writeString(localConfiguration, """
                        # Local Central Docs settings. Restart the application after changing them.
                        # app:
                        #   storage:
                        #     documents-directory: C:/Users/YourName/Documents/Central Docs/Documents
                        """, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to create the local configuration directory.", exception);
        }
        List<String> resolvedArguments = new ArrayList<>(Arrays.asList(arguments));
        resolvedArguments.add("--server.address=127.0.0.1");
        resolvedArguments.add("--server.port=" + PORT);
        resolvedArguments.add("--spring.config.additional-location=optional:" + configDirectory.toUri());
        return resolvedArguments.toArray(String[]::new);
    }

    public static void openBrowser() {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(APPLICATION_URI);
                return;
            }
            if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", APPLICATION_URI.toString())
                        .start();
                return;
            }
            LOGGER.warn("Central Docs is available at {}, but no browser launcher is available.", APPLICATION_URI);
        } catch (IOException exception) {
            LOGGER.warn("Unable to open the browser automatically. Open {} manually.", APPLICATION_URI, exception);
        }
    }
}
