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
        @JsonProperty("num_motor") @NotBlank String engineNumber,
        @JsonProperty("num_chasis") @NotBlank String chassisNumber,
        @JsonProperty("num_vin") @NotBlank String vinNumber) {
}
