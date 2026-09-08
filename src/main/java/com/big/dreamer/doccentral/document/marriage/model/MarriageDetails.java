package com.big.dreamer.doccentral.document.marriage.model;

import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarriageDetails(
        @JsonProperty("fecha_acta") @NotNull LocalDate premaritalDate,
        @JsonProperty("hora_acta") @NotBlank String premaritalTime,
        @JsonProperty("lugar_acta") @NotBlank String premaritalPlace,
        @JsonProperty("departamento_acta") @NotBlank String premaritalState,
        @JsonProperty("fecha_celebracion") @NotNull LocalDate celebrationDate,
        @JsonProperty("hora_celebracion") @NotBlank String celebrationTime,
        @JsonProperty("lugar_celebracion") @NotBlank String celebrationPlace,
        @JsonProperty("departamento_celebracion") @NotBlank String celebrationState,
        @JsonProperty("numero_escritura") @NotNull Integer deedNumber,
        @JsonProperty("regimen_patrimonial") String propertyRegime,
        @JsonProperty("capitulaciones") boolean capitulations,
        @JsonProperty("detalle_capitulaciones") String capitulationsDetails,
        @JsonProperty("nombre_posterior") String marriedName,
        @JsonProperty("matrimonio_por_poder") boolean marriageByProxy,
        @JsonProperty("detalle_poder") String proxyDetails,
        @JsonProperty("interprete") @Valid PersonDetails interpreter) {
}
