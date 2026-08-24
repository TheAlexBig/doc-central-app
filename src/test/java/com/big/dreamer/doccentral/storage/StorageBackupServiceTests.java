package com.big.dreamer.doccentral.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class StorageBackupServiceTests {

    @TempDir
    Path directory;

    @Test
    void exportsAndRestoresDataWithoutIncludingLicense() throws Exception {
        ApplicationDirectories directories = new ApplicationDirectories(
                directory.resolve("data").toString(), directory.resolve("documents").toString());
        directories.initialize();
        Files.writeString(directories.peopleFile(), "[{\"documento\":\"1\"}]");
        Files.writeString(directories.licenseFile(), "secret");
        Files.writeString(directories.documentsDirectory().resolve("sale.docx"), "document");
        Files.writeString(directories.documentsDirectory().resolve("license.json"), "receipt");
        StorageBackupService service = new StorageBackupService(directories, new ObjectMapper());

        byte[] backup = service.exportBackup();
        List<String> entries = entryNames(backup);
        assertThat(entries).contains("manifest.json", "data/people.json", "documents/sale.docx");
        assertThat(entries).noneMatch(name -> name.contains("license.json"));

        Files.writeString(directories.peopleFile(), "[]");
        service.restoreBackup(backup);
        assertThat(Files.readString(directories.peopleFile())).contains("documento");
    }

    private List<String> entryNames(byte[] archive) throws Exception {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            for (var entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) names.add(entry.getName());
        }
        return names;
    }
}
