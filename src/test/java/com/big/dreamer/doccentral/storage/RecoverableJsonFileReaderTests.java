package com.big.dreamer.doccentral.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RecoverableJsonFileReaderTests {

    @TempDir
    Path directory;

    @Test
    void restoresNewestValidBackupAndPreservesCorruptFile() throws Exception {
        Path file = directory.resolve("people.json");
        LocalJsonFileWriter.write(file, "[]");
        LocalJsonFileWriter.write(file, "[{\"name\":\"Ana\"}]");
        Files.writeString(file, "{broken");

        Entry[] result = RecoverableJsonFileReader.read(file, new ObjectMapper(), Entry[].class);

        assertThat(result).isEmpty();
        assertThat(Files.readString(file)).isEqualTo("[]");
        try (var files = Files.list(directory)) {
            assertThat(files.map(path -> path.getFileName().toString()))
                    .anyMatch(name -> name.startsWith("people.json.corrupt-"));
        }
    }

    record Entry(String name) {}
}
