package com.big.dreamer.doccentral.agent.service;

import com.big.dreamer.doccentral.agent.model.Agent;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class AgentRepository {

    private final Path agentsFile;
    private final ObjectMapper objectMapper;

    public AgentRepository(ApplicationDirectories directories, ObjectMapper objectMapper) {
        this.agentsFile = directories.agentsFile();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void initialize() {
        if (Files.notExists(agentsFile)) {
            write(List.of());
        }
    }

    public synchronized List<Agent> findAll() {
        try {
            Agent[] agents = objectMapper.readValue(Files.readString(agentsFile, StandardCharsets.UTF_8), Agent[].class);
            return List.of(agents);
        } catch (IOException exception) {
            throw new AgentStorageException("Unable to read local agents.", exception);
        }
    }

    public synchronized Agent save(Agent submittedAgent) {
        Agent agent = submittedAgent.withId(UUID.randomUUID().toString());
        List<Agent> agents = new ArrayList<>(findAll());
        agents.add(agent);
        write(agents);
        return agent;
    }

    public synchronized Optional<Agent> update(String id, Agent submittedAgent) {
        List<Agent> agents = new ArrayList<>(findAll());
        for (int index = 0; index < agents.size(); index++) {
            if (agents.get(index).id().equals(id)) {
                Agent agent = submittedAgent.withId(id);
                agents.set(index, agent);
                write(agents);
                return Optional.of(agent);
            }
        }
        return Optional.empty();
    }

    public synchronized boolean delete(String id) {
        List<Agent> agents = new ArrayList<>(findAll());
        boolean removed = agents.removeIf(agent -> agent.id().equals(id));
        if (removed) {
            write(agents);
        }
        return removed;
    }

    private void write(List<Agent> agents) {
        Path temporaryFile = agentsFile.resolveSibling(agentsFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(agentsFile.getParent());
            Files.writeString(temporaryFile, objectMapper.writeValueAsString(agents), StandardCharsets.UTF_8);
            Files.move(temporaryFile, agentsFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new AgentStorageException("Unable to save local agents.", exception);
        }
    }
}
