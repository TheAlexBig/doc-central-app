package com.big.dreamer.doccentral.document.carsale.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LegalAgentDetails(
        @JsonProperty("nombre") @NotBlank String givenName,
        @JsonProperty("apellido") @NotBlank String lastName,
        @JsonProperty("departamento") @NotBlank String state,
        @JsonProperty("domicilio") @NotBlank String settlement,
        @JsonProperty("genero") @NotBlank String gender) {
}
