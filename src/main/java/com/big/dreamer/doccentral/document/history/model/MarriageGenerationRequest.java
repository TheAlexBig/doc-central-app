package com.big.dreamer.doccentral.document.history.model;

import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record MarriageGenerationRequest(
        @JsonProperty("documento") @Valid @NotNull MarriageDocumentRequest document,
        @JsonProperty("borrador") Map<String, Object> draft) {
}
