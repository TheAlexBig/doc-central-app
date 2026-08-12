package com.big.dreamer.doccentral.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Stream;

public final class LocalJsonFileWriter {

    private static final int MAX_BACKUPS_PER_FILE = 25;
    private static final DateTimeFormatter BACKUP_TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                    .withZone(ZoneOffset.UTC);

    private LocalJsonFileWriter() {
    }

    public static void write(Path targetFile, String contents) throws IOException {
        Path temporaryFile = targetFile.resolveSibling(targetFile.getFileName() + ".tmp");
        Files.createDirectories(targetFile.getParent());
        backupExistingFile(targetFile);
        Files.writeString(temporaryFile, contents, StandardCharsets.UTF_8);
        Files.move(temporaryFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    private static void backupExistingFile(Path targetFile) throws IOException {
        if (Files.notExists(targetFile)) {
            return;
        }
        Path backupDirectory = backupDirectory(targetFile);
        Files.createDirectories(backupDirectory);
        Files.copy(
                targetFile,
                backupDirectory.resolve(backupFileName(targetFile)),
                StandardCopyOption.REPLACE_EXISTING);
        pruneBackups(backupDirectory);
    }

    private static Path backupDirectory(Path targetFile) {
        return targetFile.getParent()
                .resolve("backups")
                .resolve(targetFile.getFileName().toString());
    }

    private static String backupFileName(Path targetFile) {
        return BACKUP_TIMESTAMP.format(Instant.now()) + "-" + targetFile.getFileName();
    }

    private static void pruneBackups(Path backupDirectory) throws IOException {
        try (Stream<Path> backups = Files.list(backupDirectory)) {
            var orderedBackups = backups
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(LocalJsonFileWriter::lastModifiedTime).reversed())
                    .toList();
            for (int index = MAX_BACKUPS_PER_FILE; index < orderedBackups.size(); index++) {
                Files.deleteIfExists(orderedBackups.get(index));
            }
        }
    }

    private static Instant lastModifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException exception) {
            return Instant.EPOCH;
        }
    }
}
