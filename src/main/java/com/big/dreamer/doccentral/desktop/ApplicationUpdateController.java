package com.big.dreamer.doccentral.desktop;

import org.springframework.http.HttpStatus;
import org.springframework.boot.info.BuildProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/v1/application")
public class ApplicationUpdateController {

    private static final String RELEASES_URL = "https://github.com/TheAlexBig/doc-central-app/releases";
    private static final URI LATEST_RELEASE_API = URI.create(
            "https://api.github.com/repos/TheAlexBig/doc-central-app/releases/latest");
    private final ObjectMapper objectMapper;
    private final ObjectProvider<BuildProperties> buildProperties;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    public ApplicationUpdateController(ObjectMapper objectMapper, ObjectProvider<BuildProperties> buildProperties) {
        this.objectMapper = objectMapper;
        this.buildProperties = buildProperties;
    }

    @GetMapping("/info")
    public ApplicationInfo info() {
        return new ApplicationInfo(currentVersion(), RELEASES_URL);
    }

    @GetMapping("/updates")
    public UpdateStatus updates() {
        String current = currentVersion();
        try {
            HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE_API)
                    .timeout(Duration.ofSeconds(8))
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Central-Docs/" + current)
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) throw new IOException("GitHub returned " + response.statusCode());
            var json = objectMapper.readTree(response.body());
            String latest = json.get("tag_name").asText().replaceFirst("^[vV]", "");
            String url = json.get("html_url").asText(RELEASES_URL);
            return new UpdateStatus(current, latest, compareVersions(latest, current) > 0, url);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "No se pudo comprobar si hay actualizaciones.", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Se interrumpió la comprobación de actualizaciones.", exception);
        }
    }

    private String currentVersion() {
        BuildProperties properties = buildProperties.getIfAvailable();
        return properties == null ? "desarrollo" : properties.getVersion();
    }

    private int compareVersions(String left, String right) {
        String[] a = left.split("\\.");
        String[] b = right.split("\\.");
        for (int index = 0; index < Math.max(a.length, b.length); index++) {
            int av = index < a.length ? number(a[index]) : 0;
            int bv = index < b.length ? number(b[index]) : 0;
            if (av != bv) return Integer.compare(av, bv);
        }
        return 0;
    }

    private int number(String value) {
        try { return Integer.parseInt(value.replaceAll("[^0-9].*$", "")); }
        catch (NumberFormatException exception) { return 0; }
    }

    public record ApplicationInfo(String version, String releasesUrl) {}
    public record UpdateStatus(String currentVersion, String latestVersion,
                               boolean updateAvailable, String releaseUrl) {}
}
