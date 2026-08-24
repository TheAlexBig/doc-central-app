package com.big.dreamer.doccentral.desktop;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SupportPackageServiceTests {

    @TempDir
    Path directory;

    @Test
    void includesOnlySanitizedDiagnosticsAndLogs() throws Exception {
        Files.writeString(directory.resolve("central-docs.log"),
                "User " + System.getProperty("user.home")
                        + " DUI 01234567-8 machine CD-0123456789ABCDEF01234567"
                        + " email person@example.com \"signature\":\"secret\"");
        ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        SupportPackageService service = new SupportPackageService(directory.toString(), provider);

        Map<String, String> files = unzip(service.create());

        assertThat(files).containsKeys("diagnostics.txt", "central-docs.log");
        assertThat(files.keySet()).noneMatch(name -> name.contains("license") || name.contains("document"));
        assertThat(files.get("central-docs.log"))
                .contains("<USER_HOME>", "<DUI_REDACTED>", "CD-<REDACTED>",
                        "<EMAIL_REDACTED>", "<REDACTED>")
                .doesNotContain("01234567-8", "person@example.com", "secret");
    }

    private Map<String, String> unzip(byte[] archive) throws Exception {
        Map<String, String> files = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                files.put(entry.getName(), new String(zip.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return files;
    }
}
