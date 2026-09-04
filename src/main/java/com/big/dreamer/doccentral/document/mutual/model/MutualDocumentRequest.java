package com.big.dreamer.doccentral.document.mutual.model;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MutualDocumentRequest(
        @JsonProperty("deudor") @Valid @NotNull PersonDetails debtor,
        @JsonProperty("acreedor") @Valid @NotNull PersonDetails creditor,
        @JsonProperty("condiciones") @Valid @NotNull MutualTerms terms,
        @JsonProperty("agente_juridico") @Valid @NotNull LegalAgentDetails legalAgent) {
}
