package com.big.dreamer.doccentral.document.history.model;

import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MutualGenerationRequest(
        @JsonProperty("documento") @Valid @NotNull MutualDocumentRequest document,
        @JsonProperty("borrador") Map<String, Object> draft) {
}
