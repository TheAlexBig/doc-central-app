package com.big.dreamer.doccentral.storage;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.stream.Stream;

public final class RecoverableJsonFileReader {

    private RecoverableJsonFileReader() {
    }

    public static <T> T read(Path file, ObjectMapper objectMapper, Class<T> type) throws IOException {
        try {
            return parse(file, objectMapper, type);
        } catch (Exception originalFailure) {
            Path backup = newestValidBackup(file, objectMapper, type);
            if (backup == null) {
                if (originalFailure instanceof IOException ioException) {
                    throw ioException;
                }
                throw new IOException("The JSON file is invalid.", originalFailure);
            }
            preserveCorruptFile(file);
            Files.copy(backup, file, StandardCopyOption.REPLACE_EXISTING);
            return parse(file, objectMapper, type);
        }
    }

    private static <T> T parse(Path file, ObjectMapper objectMapper, Class<T> type) throws IOException {
        return objectMapper.readValue(Files.readString(file, StandardCharsets.UTF_8), type);
    }

    private static <T> Path newestValidBackup(Path file, ObjectMapper objectMapper, Class<T> type)
            throws IOException {
        Path directory = file.getParent().resolve("backups").resolve(file.getFileName().toString());
        if (Files.notExists(directory)) {
            return null;
        }
        try (Stream<Path> backups = Files.list(directory)) {
            for (Path backup : backups.filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(RecoverableJsonFileReader::modified).reversed())
                    .toList()) {
                try {
                    parse(backup, objectMapper, type);
                    return backup;
                } catch (Exception ignored) {
                    // Try the next older backup.
                }
            }
        }
        return null;
    }

    private static void preserveCorruptFile(Path file) throws IOException {
        if (Files.exists(file)) {
            Files.move(file, file.resolveSibling(file.getFileName() + ".corrupt-" + Instant.now().toEpochMilli()),
                    StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Instant modified(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException exception) {
            return Instant.EPOCH;
        }
    }
}
