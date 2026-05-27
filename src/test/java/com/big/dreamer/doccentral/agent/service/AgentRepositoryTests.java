package com.big.dreamer.doccentral.agent.service;

import com.big.dreamer.doccentral.agent.model.Agent;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRepositoryTests {

    @TempDir
    Path temporaryDirectory;

    private ApplicationDirectories directories;
    private AgentRepository repository;

    @BeforeEach
    void setUp() {
        directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        repository = new AgentRepository(directories, new ObjectMapper());
        repository.initialize();
    }

    @Test
    void initializesAnEmptyLocalAgentFile() throws Exception {
        assertThat(repository.findAll()).isEmpty();
        assertThat(Files.readString(directories.agentsFile())).isEqualTo("[]");
    }

    @Test
    void savesAndDeletesAnAgentInTheLocalFile() {
        Agent saved = repository.save(new Agent(
                null, "Ana", "Lopez", "La Libertad", "La Libertad Sur",
                "Santa Tecla", "00000000", "Femenino"));

        assertThat(saved.id()).isNotBlank();
        assertThat(repository.findAll()).containsExactly(saved);

        Agent updated = repository.update(saved.id(), new Agent(
                null, "Ana", "Perez", "La Libertad", "La Libertad Sur",
                "Santa Tecla", "00000000", "Femenino")).orElseThrow();

        assertThat(updated.id()).isEqualTo(saved.id());
        assertThat(repository.findAll()).containsExactly(updated);

        assertThat(repository.delete(saved.id())).isTrue();
        assertThat(repository.findAll()).isEmpty();
    }
}
