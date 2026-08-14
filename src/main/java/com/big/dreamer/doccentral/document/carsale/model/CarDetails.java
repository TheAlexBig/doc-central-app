package com.big.dreamer.doccentral.document.carsale.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record CarDetails(
        @JsonProperty("placa") @NotBlank String licensePlate,
        @JsonProperty("marca") @NotBlank String brand,
        @JsonProperty("modelo") @NotBlank String model,
        @JsonProperty("color") @NotBlank String color,
        @JsonProperty("fabricado") @NotBlank String factoryYear,
        @JsonProperty("capacidad") @NotBlank String capacity,
        @JsonProperty("dominio") @NotBlank String domain,
        @JsonProperty("clase") @NotBlank String vehicleClass,
        @JsonProperty("tipo") @NotBlank String vehicleType,
        @JsonProperty("ejes") String axles,
        @JsonProperty("tara") String tare,
        @JsonProperty("tipo_capacidad") String capacityType,
        @JsonProperty("cap_carga") String loadCapacity,
        @JsonProperty("cap_maxima") String maximumCapacity,
        @JsonProperty("traccion") String traction,
        @JsonProperty("num_motor") @NotBlank String engineNumber,
        @JsonProperty("num_chasis") @NotBlank String chassisNumber,
        @JsonProperty("num_vin") @NotBlank String vinNumber) {

    public CarDetails(
            String licensePlate, String brand, String model, String color, String factoryYear,
            String capacity, String domain, String vehicleClass, String vehicleType,
            String engineNumber, String chassisNumber, String vinNumber) {
        this(licensePlate, brand, model, color, factoryYear, capacity, domain, vehicleClass, vehicleType,
                null, null, null, null, null, null, engineNumber, chassisNumber, vinNumber);
    }
}
