package com.big.dreamer.doccentral.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LocalJsonFileWriterTests {

    @TempDir
    Path temporaryDirectory;

    @Test
    void backsUpExistingJsonBeforeReplacingIt() throws Exception {
        Path file = temporaryDirectory.resolve("people.json");

        LocalJsonFileWriter.write(file, "[]");
        LocalJsonFileWriter.write(file, "[{\"nombre\":\"Ana\"}]");

        Path backupDirectory = temporaryDirectory.resolve("backups").resolve("people.json");
        assertThat(Files.readString(file, StandardCharsets.UTF_8)).contains("Ana");
        try (var backups = Files.list(backupDirectory)) {
            assertThat(backups.toList())
                .singleElement()
                .satisfies(backup ->
                        assertThat(Files.readString(backup, StandardCharsets.UTF_8)).isEqualTo("[]"));
        }
    }

    @Test
    void keepsOnlyTheMostRecentBackups() throws Exception {
        Path file = temporaryDirectory.resolve("vehicles.json");

        for (int index = 0; index < 30; index++) {
            LocalJsonFileWriter.write(file, "[{\"index\":" + index + "}]");
            Thread.sleep(2);
        }

        Path backupDirectory = temporaryDirectory.resolve("backups").resolve("vehicles.json");
        try (var backups = Files.list(backupDirectory)) {
            List<Path> backupFiles = backups.toList();
            assertThat(backupFiles).hasSize(25);
        }
    }
}
