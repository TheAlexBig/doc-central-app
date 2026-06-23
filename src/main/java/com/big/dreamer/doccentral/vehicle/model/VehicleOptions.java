package com.big.dreamer.doccentral.vehicle.model;

import java.util.List;
import java.util.Map;

public record VehicleOptions(
        List<String> colors,
        List<String> brands,
        List<String> models,
        Map<String, List<String>> modelsByBrand) {
}
