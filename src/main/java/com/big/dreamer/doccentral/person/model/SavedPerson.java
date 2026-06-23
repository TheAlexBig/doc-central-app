package com.big.dreamer.doccentral.person.model;

import jakarta.validation.constraints.NotBlank;

public record SavedPerson(
        String id,
        @NotBlank String nombre,
        @NotBlank String apellido,
        @NotBlank String departamento,
        @NotBlank String municipio,
        @NotBlank String domicilio,
        @NotBlank String fecha_nacimiento,
        @NotBlank String documento,
        @NotBlank String genero,
        @NotBlank String oficio,
        String rememberedAt) {

    public SavedPerson withMemoryFields(String generatedId, String rememberedAt) {
        return new SavedPerson(
                generatedId,
                nombre,
                apellido,
                departamento,
                municipio,
                domicilio,
                fecha_nacimiento,
                documento,
                genero,
                oficio,
                rememberedAt);
    }
}
