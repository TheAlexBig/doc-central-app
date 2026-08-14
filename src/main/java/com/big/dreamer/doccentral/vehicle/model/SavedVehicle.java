package com.big.dreamer.doccentral.vehicle.model;

import jakarta.validation.constraints.NotBlank;

public record SavedVehicle(
        String id,
        @NotBlank String placa,
        @NotBlank String marca,
        @NotBlank String modelo,
        @NotBlank String color,
        @NotBlank String fabricado,
        @NotBlank String capacidad,
        @NotBlank String unidad_capacidad,
        @NotBlank String dominio,
        @NotBlank String clase,
        @NotBlank String tipo,
        @NotBlank String num_motor,
        @NotBlank String num_chasis,
        @NotBlank String num_vin,
        String rememberedAt) {

    public SavedVehicle withMemoryFields(String generatedId, String rememberedAt) {
        return new SavedVehicle(
                generatedId,
                placa,
                marca,
                modelo,
                color,
                fabricado,
                capacidad,
                unidad_capacidad,
                dominio,
                clase,
                tipo,
                num_motor,
                num_chasis,
                num_vin,
                rememberedAt);
    }
}
