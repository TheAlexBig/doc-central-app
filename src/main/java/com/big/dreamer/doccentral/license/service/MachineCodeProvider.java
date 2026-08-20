package com.big.dreamer.doccentral.license.service;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
public class MachineCodeProvider {

    public String machineCode() {
        return "CD-" + hash(machineIdentity()).substring(0, 24).toUpperCase();
    }

    private String machineIdentity() {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                Process process = new ProcessBuilder(
                        "reg", "query", "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid")
                        .redirectErrorStream(true)
                        .start();
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (process.waitFor() == 0 && output.contains("REG_SZ")) {
                    return output.substring(output.indexOf("REG_SZ") + 6).trim();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (Exception ignored) {
                // Fall back to the local host identity when the registry is unavailable.
            }
        }
        try {
            return System.getProperty("user.name", "") + "|"
                    + System.getProperty("os.name", "") + "|"
                    + InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return System.getProperty("user.name", "") + "|" + System.getProperty("os.name", "");
        }
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new LicenseException("No se pudo calcular el código del equipo.", exception);
        }
    }
}
