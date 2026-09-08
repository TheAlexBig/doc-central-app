package com.big.dreamer.doccentral.document.marriage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RecognizedChild(
        @JsonProperty("nombre") @NotBlank String name,
        @JsonProperty("partida_nacimiento") @NotBlank String birthCertificate) {
}
