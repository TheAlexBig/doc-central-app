package com.big.dreamer.doccentral.document.carsale.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CarSaleDocumentRequest(
        @JsonProperty("vendedor") @Valid @NotNull PersonDetails seller,
        @JsonProperty("comprador") @Valid @NotNull PersonDetails buyer,
        @JsonProperty("vehiculo") @Valid @NotNull CarDetails vehicle,
        @JsonProperty("documento") @Valid @NotNull DocumentDetails document,
        @JsonProperty("agente_juridico") @Valid @NotNull LegalAgentDetails legalAgent) {
}
