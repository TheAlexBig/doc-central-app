package com.big.dreamer.doccentral.document.marriage.model;

import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarriageWitness(
        @JsonProperty("persona") @Valid @NotNull PersonDetails person,
        @JsonProperty("fecha_nacimiento") @NotNull LocalDate birthDate,
        @JsonProperty("lee_escribe_castellano") boolean readsWritesSpanish,
        @JsonProperty("conoce_contrayentes") boolean knowsParties,
        @JsonProperty("relacion_prohibida") boolean prohibitedRelationship) {
}
