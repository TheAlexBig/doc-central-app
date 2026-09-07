package com.big.dreamer.doccentral.document.mutual.model;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MutualVehiclePledge(
        @JsonProperty("vehiculo") @Valid @NotNull CarDetails vehicle,
        @JsonProperty("valor") @NotBlank String valuation) {
}
