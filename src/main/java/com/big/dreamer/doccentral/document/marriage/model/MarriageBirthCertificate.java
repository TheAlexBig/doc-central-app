package com.big.dreamer.doccentral.document.marriage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarriageBirthCertificate(
        @JsonProperty("numero") @NotBlank String number,
        @JsonProperty("folio") String folio,
        @JsonProperty("libro") String book,
        @JsonProperty("registro") @NotBlank String registry,
        @JsonProperty("fecha_expedicion") @NotNull LocalDate issueDate,
        @JsonProperty("expedida_por") @NotBlank String issuedBy) {
}
