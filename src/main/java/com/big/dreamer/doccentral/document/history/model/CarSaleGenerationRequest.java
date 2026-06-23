package com.big.dreamer.doccentral.document.history.model;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CarSaleGenerationRequest(
        @JsonProperty("documento") @Valid @NotNull CarSaleDocumentRequest document,
        @JsonProperty("borrador") Map<String, Object> draft) {
}
