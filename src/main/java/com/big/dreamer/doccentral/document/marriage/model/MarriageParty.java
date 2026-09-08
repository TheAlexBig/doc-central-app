package com.big.dreamer.doccentral.document.marriage.model;

import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record MarriageParty(
        @JsonProperty("persona") @Valid @NotNull PersonDetails person,
        @JsonProperty("fecha_nacimiento") @NotNull LocalDate birthDate,
        @JsonProperty("estado_familiar") @NotBlank String familyStatus,
        @JsonProperty("nacionalidad") @NotBlank String nationality,
        @JsonProperty("lugar_nacimiento") @NotBlank String birthPlace,
        @JsonProperty("tipo_identificacion") @NotBlank String identityType,
        @JsonProperty("madre") @Valid @NotNull MarriageParent mother,
        @JsonProperty("padre") @Valid @NotNull MarriageParent father,
        @JsonProperty("partida_nacimiento") @Valid @NotNull MarriageBirthCertificate birthCertificate,
        @JsonProperty("documento_estado_familiar") String familyStatusDocument,
        @JsonProperty("vinculo_matrimonial_vigente") boolean currentMarriage,
        @JsonProperty("puede_consentir") boolean canConsent,
        @JsonProperty("parentesco_impediente") boolean prohibitedKinship,
        @JsonProperty("relacion_adopcion_impediente") boolean prohibitedAdoptionRelationship,
        @JsonProperty("restriccion_homicidio") boolean spouseHomicideRestriction,
        @JsonProperty("restriccion_tutela") boolean tutorRestriction,
        @JsonProperty("se_expresa_castellano") boolean speaksSpanish) {
}
