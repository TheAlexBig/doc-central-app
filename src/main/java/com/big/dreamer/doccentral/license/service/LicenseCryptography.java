package com.big.dreamer.doccentral.license.service;

import com.big.dreamer.doccentral.license.model.LicenseDocument;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public final class LicenseCryptography {

    private LicenseCryptography() {
    }

    public static String payload(LicenseDocument license) {
        return String.join("\n",
                license.licenseId(), license.customer(), license.machineCode(), license.issuedAt());
    }

    public static String sign(LicenseDocument license, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(privateKey);
            signer.update(payload(license).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (Exception exception) {
            throw new LicenseException("No se pudo firmar la licencia.", exception);
        }
    }

    public static boolean verify(LicenseDocument license, PublicKey publicKey) {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(publicKey);
            verifier.update(payload(license).getBytes(StandardCharsets.UTF_8));
            return verifier.verify(Base64.getDecoder().decode(license.signature()));
        } catch (Exception exception) {
            return false;
        }
    }
}
