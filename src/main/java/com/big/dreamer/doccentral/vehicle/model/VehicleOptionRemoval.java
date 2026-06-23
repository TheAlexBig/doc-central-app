package com.big.dreamer.doccentral.vehicle.model;

import jakarta.validation.constraints.NotBlank;

public record VehicleOptionRemoval(
        @NotBlank String kind,
        @NotBlank String value) {
}
