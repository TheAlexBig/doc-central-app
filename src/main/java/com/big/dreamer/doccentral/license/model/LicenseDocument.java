package com.big.dreamer.doccentral.license.model;

public record LicenseDocument(
        String licenseId,
        String customer,
        String machineCode,
        String issuedAt,
        String signature) {
}
