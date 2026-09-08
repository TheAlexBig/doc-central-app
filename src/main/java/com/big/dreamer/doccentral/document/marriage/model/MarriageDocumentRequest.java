package com.big.dreamer.doccentral.document.marriage.model;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MarriageDocumentRequest(
        @JsonProperty("contrayente_uno") @Valid @NotNull MarriageParty partyOne,
        @JsonProperty("contrayente_dos") @Valid @NotNull MarriageParty partyTwo,
        @JsonProperty("testigos") @Valid @NotNull @Size(min = 2) List<MarriageWitness> witnesses,
        @JsonProperty("hijos_reconocidos") @Valid List<RecognizedChild> recognizedChildren,
        @JsonProperty("datos") @Valid @NotNull MarriageDetails details,
        @JsonProperty("agente_juridico") @Valid @NotNull LegalAgentDetails legalAgent) {

    public MarriageDocumentRequest {
        recognizedChildren = recognizedChildren == null ? List.of() : List.copyOf(recognizedChildren);
        witnesses = witnesses == null ? List.of() : List.copyOf(witnesses);
    }
}
