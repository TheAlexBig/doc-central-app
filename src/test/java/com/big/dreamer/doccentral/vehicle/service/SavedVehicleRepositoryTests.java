package com.big.dreamer.doccentral.vehicle.service;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.vehicle.model.SavedVehicle;
import com.big.dreamer.doccentral.vehicle.model.VehicleOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SavedVehicleRepositoryTests {

    @TempDir
    Path temporaryDirectory;

    private ApplicationDirectories directories;
    private SavedVehicleRepository repository;

    @BeforeEach
    void setUp() {
        directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        repository = new SavedVehicleRepository(directories, new ObjectMapper());
        repository.initialize();
    }

    @Test
    void initializesAnEmptyLocalVehiclesFile() throws Exception {
        assertThat(repository.findAll()).isEmpty();
        assertThat(Files.readString(directories.vehiclesFile())).isEqualTo("[]");
    }

    @Test
    void savesAVehicleAndBuildsOptions() {
        SavedVehicle saved = repository.save(new SavedVehicle(
                null, "P-123456", "Toyota", "Corolla Cross",
                "Gris", "2022-01-01", "5", "Propiedad", "Automovil",
                "ABC123", "DEF456", "GHI789", null));

        assertThat(saved.id()).isEqualTo("P123456");
        assertThat(saved.rememberedAt()).isNotBlank();

        VehicleOptions options = repository.findOptions();
        assertThat(options.colors()).containsExactly("Gris");
        assertThat(options.brands()).containsExactly("Toyota");
        assertThat(options.models()).containsExactly("Corolla Cross");
        assertThat(options.modelsByBrand()).containsEntry("Toyota", List.of("Corolla Cross"));
    }
}
