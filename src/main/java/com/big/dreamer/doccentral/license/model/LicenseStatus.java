package com.big.dreamer.doccentral.license.model;

public record LicenseStatus(
        boolean active,
        String machineCode,
        String licenseId,
        String customer,
        String message) {
}
