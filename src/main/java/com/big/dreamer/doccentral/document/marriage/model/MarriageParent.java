package com.big.dreamer.doccentral.document.marriage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record MarriageParent(
        @JsonProperty("nombre") @NotBlank String name,
        @JsonProperty("oficio") @NotBlank String job,
        @JsonProperty("domicilio") @NotBlank String settlement) {
}
