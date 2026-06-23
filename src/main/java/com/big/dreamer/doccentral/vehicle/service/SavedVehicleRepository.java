package com.big.dreamer.doccentral.vehicle.service;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.vehicle.model.VehicleOptionExclusions;
import com.big.dreamer.doccentral.vehicle.model.SavedVehicle;
import com.big.dreamer.doccentral.vehicle.model.VehicleOptions;
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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class SavedVehicleRepository {

    private final Path vehiclesFile;
    private final Path optionExclusionsFile;
    private final ObjectMapper objectMapper;

    public SavedVehicleRepository(ApplicationDirectories directories, ObjectMapper objectMapper) {
        this.vehiclesFile = directories.vehiclesFile();
        this.optionExclusionsFile = directories.vehicleOptionExclusionsFile();
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public synchronized void initialize() {
        if (Files.notExists(vehiclesFile)) {
            write(List.of());
        }
        if (Files.notExists(optionExclusionsFile)) {
            writeExclusions(VehicleOptionExclusions.empty());
        }
    }

    public synchronized List<SavedVehicle> findAll() {
        try {
            SavedVehicle[] vehicles = objectMapper.readValue(
                    Files.readString(vehiclesFile, StandardCharsets.UTF_8),
                    SavedVehicle[].class);
            return sortByMostRecent(List.of(vehicles));
        } catch (IOException exception) {
            throw new VehicleStorageException("Unable to read local vehicles.", exception);
        }
    }

    public synchronized VehicleOptions findOptions() {
        LinkedHashSet<String> colors = new LinkedHashSet<>();
        LinkedHashSet<String> brands = new LinkedHashSet<>();
        LinkedHashSet<String> models = new LinkedHashSet<>();
        Map<String, LinkedHashSet<String>> modelsByBrand = new LinkedHashMap<>();
        VehicleOptionExclusions exclusions = findExclusions();

        findAll().forEach(vehicle -> {
            addIfPresent(colors, vehicle.color(), exclusions.colors());
            addIfPresent(brands, vehicle.marca(), exclusions.brands());
            addIfPresent(models, vehicle.modelo(), exclusions.models());

            String brand = cleanText(vehicle.marca());
            String model = cleanText(vehicle.modelo());
            if (!brand.isBlank()
                    && !model.isBlank()
                    && !containsOption(exclusions.brands(), brand)
                    && !containsOption(exclusions.models(), model)) {
                modelsByBrand.computeIfAbsent(brand, key -> new LinkedHashSet<>()).add(model);
            }
        });

        Map<String, List<String>> modelOptionsByBrand = new LinkedHashMap<>();
        modelsByBrand.forEach((brand, brandModels) ->
                modelOptionsByBrand.put(brand, List.copyOf(brandModels)));

        return new VehicleOptions(
                List.copyOf(colors),
                List.copyOf(brands),
                List.copyOf(models),
                modelOptionsByBrand);
    }

    public synchronized SavedVehicle save(SavedVehicle submittedVehicle) {
        String plate = normalizePlate(submittedVehicle.placa());
        List<SavedVehicle> vehicles = new ArrayList<>(findAll());
        SavedVehicle savedVehicle = cleanVehicle(submittedVehicle)
                .withMemoryFields(plate, Instant.now().toString());

        vehicles.removeIf(vehicle -> normalizePlate(vehicle.placa()).equals(plate));
        vehicles.add(0, savedVehicle);
        write(sortByMostRecent(vehicles));
        return savedVehicle;
    }

    public synchronized VehicleOptions forgetOption(String kind, String value) {
        VehicleOptionExclusions exclusions = findExclusions();
        String cleanedValue = cleanText(value);
        VehicleOptionExclusions nextExclusions = switch (kind.toLowerCase()) {
            case "color", "colors" -> new VehicleOptionExclusions(
                    withOption(exclusions.colors(), cleanedValue),
                    exclusions.brands(),
                    exclusions.models());
            case "brand", "brands", "marca", "marcas" -> new VehicleOptionExclusions(
                    exclusions.colors(),
                    withOption(exclusions.brands(), cleanedValue),
                    exclusions.models());
            case "model", "models", "modelo", "modelos" -> new VehicleOptionExclusions(
                    exclusions.colors(),
                    exclusions.brands(),
                    withOption(exclusions.models(), cleanedValue));
            default -> exclusions;
        };
        writeExclusions(nextExclusions);
        return findOptions();
    }

    private SavedVehicle cleanVehicle(SavedVehicle vehicle) {
        return new SavedVehicle(
                vehicle.id(),
                cleanText(vehicle.placa()).toUpperCase(),
                cleanText(vehicle.marca()),
                cleanText(vehicle.modelo()),
                cleanText(vehicle.color()),
                cleanText(vehicle.fabricado()),
                cleanText(vehicle.capacidad()),
                cleanText(vehicle.dominio()),
                cleanText(vehicle.clase()),
                cleanText(vehicle.num_motor()).toUpperCase(),
                cleanText(vehicle.num_chasis()).toUpperCase(),
                cleanText(vehicle.num_vin()).toUpperCase(),
                vehicle.rememberedAt());
    }

    private void addIfPresent(LinkedHashSet<String> values, String value, Set<String> exclusions) {
        String cleanedValue = cleanText(value);
        if (!cleanedValue.isBlank() && !containsOption(exclusions, cleanedValue)) {
            values.add(cleanedValue);
        }
    }

    private Set<String> withOption(Set<String> values, String value) {
        LinkedHashSet<String> nextValues = new LinkedHashSet<>(values);
        if (!value.isBlank()) {
            nextValues.add(value);
        }
        return nextValues;
    }

    private boolean containsOption(Set<String> values, String value) {
        return values.stream().anyMatch(currentValue -> currentValue.equalsIgnoreCase(value));
    }

    private List<SavedVehicle> sortByMostRecent(List<SavedVehicle> vehicles) {
        return vehicles.stream()
                .sorted(Comparator.comparing(
                        SavedVehicle::rememberedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String normalizePlate(String plate) {
        return cleanText(plate).toUpperCase().replaceAll("[^0-9A-Z]", "");
    }

    private void write(List<SavedVehicle> vehicles) {
        Path temporaryFile = vehiclesFile.resolveSibling(vehiclesFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(vehiclesFile.getParent());
            Files.writeString(temporaryFile, objectMapper.writeValueAsString(vehicles), StandardCharsets.UTF_8);
            Files.move(temporaryFile, vehiclesFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new VehicleStorageException("Unable to save local vehicles.", exception);
        }
    }

    private VehicleOptionExclusions findExclusions() {
        try {
            return objectMapper.readValue(
                    Files.readString(optionExclusionsFile, StandardCharsets.UTF_8),
                    VehicleOptionExclusions.class);
        } catch (IOException exception) {
            throw new VehicleStorageException("Unable to read local vehicle option settings.", exception);
        }
    }

    private void writeExclusions(VehicleOptionExclusions exclusions) {
        Path temporaryFile = optionExclusionsFile.resolveSibling(optionExclusionsFile.getFileName() + ".tmp");
        try {
            Files.createDirectories(optionExclusionsFile.getParent());
            Files.writeString(temporaryFile, objectMapper.writeValueAsString(exclusions), StandardCharsets.UTF_8);
            Files.move(temporaryFile, optionExclusionsFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new VehicleStorageException("Unable to save local vehicle option settings.", exception);
        }
    }
}
