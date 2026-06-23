package com.big.dreamer.doccentral.vehicle.model;

import java.util.Set;

public record VehicleOptionExclusions(
        Set<String> colors,
        Set<String> brands,
        Set<String> models) {

    public static VehicleOptionExclusions empty() {
        return new VehicleOptionExclusions(Set.of(), Set.of(), Set.of());
    }
}
