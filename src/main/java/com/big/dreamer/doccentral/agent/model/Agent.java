package com.big.dreamer.doccentral.agent.model;

import jakarta.validation.constraints.NotBlank;

public record Agent(
        String id,
        @NotBlank String nombres,
        @NotBlank String apellidos,
        @NotBlank String departamento,
        @NotBlank String municipio,
        @NotBlank String distrito,
        @NotBlank String carnet,
        @NotBlank String genero) {

    public Agent withId(String generatedId) {
        return new Agent(generatedId, nombres, apellidos, departamento, municipio, distrito, carnet, genero);
    }
}
