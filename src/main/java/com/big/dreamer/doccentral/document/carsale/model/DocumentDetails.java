package com.big.dreamer.doccentral.document.carsale.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record DocumentDetails(
        @JsonProperty("calidad_de") @NotBlank String garment,
        @JsonProperty("institucion") String institution,
        @JsonProperty("precio") @NotBlank String price,
        @JsonProperty("domicilio") @NotBlank String settlement,
        @JsonProperty("departamento") @NotBlank String state,
        @JsonProperty("fecha_firma") @NotBlank String signDate,
        @JsonProperty("hora_firma") @NotBlank String signHour,
        @JsonProperty("identifica_vendedor") @NotBlank String identifiesSeller,
        @JsonProperty("identifica_comprador") @NotBlank String identifiesBuyer) {
}
