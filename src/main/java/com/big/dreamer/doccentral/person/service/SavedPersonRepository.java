package com.big.dreamer.doccentral.person.service;

import com.big.dreamer.doccentral.person.model.SavedPerson;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Repository
public class SavedPersonRepository {

    private final Path peopleFile;
    private final ObjectMapper objectMapper;

    public SavedPersonRepository(ApplicationDirectories directories, ObjectMapper objectMapper) {
        this.peopleFile = directories.peopleFile();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void initialize() {
        if (Files.notExists(peopleFile)) {
            write(List.of());
        }
    }

    public synchronized List<SavedPerson> findAll() {
        try {
            SavedPerson[] people = objectMapper.readValue(
                    Files.readString(peopleFile, StandardCharsets.UTF_8),
                    SavedPerson[].class);
            return sortByMostRecent(List.of(people));
        } catch (IOException exception) {
            throw new PersonStorageException("Unable to read local people.", exception);
        }
    }

    public synchronized List<String> findOccupations() {
        LinkedHashSet<String> occupations = new LinkedHashSet<>();
        findAll().stream()
                .map(SavedPerson::oficio)
                .filter(occupation -> occupation != null && !occupation.isBlank())
                .forEach(occupations::add);
        return List.copyOf(occupations);
    }

    public synchronized SavedPerson save(SavedPerson submittedPerson) {
        String dui = normalizeDui(submittedPerson.documento());
        List<SavedPerson> people = new ArrayList<>(findAll());
        SavedPerson savedPerson = submittedPerson.withMemoryFields(
                dui,
                Instant.now().toString());

        people.removeIf(person -> normalizeDui(person.documento()).equals(dui));
        people.add(0, savedPerson);
        write(sortByMostRecent(people));
        return savedPerson;
    }

    public synchronized boolean delete(String documento) {
        String dui = normalizeDui(documento);
        List<SavedPerson> people = new ArrayList<>(findAll());
        boolean removed = people.removeIf(person -> normalizeDui(person.documento()).equals(dui));
        if (removed) {
            write(sortByMostRecent(people));
        }
        return removed;
    }

    private List<SavedPerson> sortByMostRecent(List<SavedPerson> people) {
        return people.stream()
                .sorted(Comparator.comparing(
                        SavedPerson::rememberedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String normalizeDui(String documento) {
        return documento == null
                ? ""
                : documento.trim().toUpperCase().replaceAll("[^0-9A-Z]", "");
    }

    private void write(List<SavedPerson> people) {
        Path temporaryFile = peopleFile.resolveSibling(peopleFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(peopleFile.getParent());
            Files.writeString(temporaryFile, objectMapper.writeValueAsString(people), StandardCharsets.UTF_8);
            Files.move(temporaryFile, peopleFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new PersonStorageException("Unable to save local people.", exception);
        }
    }
}
