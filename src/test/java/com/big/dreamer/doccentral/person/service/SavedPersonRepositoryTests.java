package com.big.dreamer.doccentral.person.service;

import com.big.dreamer.doccentral.person.model.SavedPerson;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SavedPersonRepositoryTests {

    @TempDir
    Path temporaryDirectory;

    private ApplicationDirectories directories;
    private SavedPersonRepository repository;

    @BeforeEach
    void setUp() {
        directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        repository = new SavedPersonRepository(directories, new ObjectMapper());
        repository.initialize();
    }

    @Test
    void initializesAnEmptyLocalPeopleFile() throws Exception {
        assertThat(repository.findAll()).isEmpty();
        assertThat(Files.readString(directories.peopleFile())).isEqualTo("[]");
    }

    @Test
    void savesAndUpdatesAPersonByDui() {
        SavedPerson saved = repository.save(new SavedPerson(
                null, "Ana", "Lopez", "La Libertad", "La Libertad Sur",
                "Santa Tecla", "1990-01-01", "00000000-0", "Femenino",
                "Comerciante", null));

        assertThat(saved.id()).isEqualTo("000000000");
        assertThat(saved.rememberedAt()).isNotNull();
        assertThat(repository.findAll()).containsExactly(saved);

        SavedPerson updated = repository.save(new SavedPerson(
                null, "Ana", "Perez", "La Libertad", "La Libertad Sur",
                "Santa Tecla", "1990-01-01", "000000000", "Femenino",
                "Abogada", null));

        assertThat(repository.findAll()).containsExactly(updated);
        assertThat(repository.findOccupations()).containsExactly("Abogada");
    }
}
