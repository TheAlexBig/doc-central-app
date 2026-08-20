package com.big.dreamer.doccentral.license.service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public final class PemKeys {

    private PemKeys() {
    }

    public static PublicKey publicKey(byte[] pem) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decode(pem)));
        } catch (Exception exception) {
            throw new LicenseException("La clave pública de licencias no es válida.", exception);
        }
    }

    public static PrivateKey privateKey(byte[] pem) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decode(pem)));
        } catch (Exception exception) {
            throw new LicenseException("La clave privada de licencias no es válida.", exception);
        }
    }

    private static byte[] decode(byte[] pem) {
        String encoded = new String(pem, StandardCharsets.US_ASCII)
                .replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(encoded);
    }
}
