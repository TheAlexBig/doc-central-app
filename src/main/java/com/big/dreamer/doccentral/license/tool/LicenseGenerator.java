package com.big.dreamer.doccentral.license.tool;

import com.big.dreamer.doccentral.license.model.LicenseDocument;
import com.big.dreamer.doccentral.license.service.LicenseCryptography;
import com.big.dreamer.doccentral.license.service.PemKeys;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

public final class LicenseGenerator {

    private LicenseGenerator() {
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String> options = options(args);
        String customer = required(options, "customer");
        String machine = required(options, "machine").toUpperCase();
        Path output = Path.of(required(options, "output"));
        Path privateKey = Path.of(options.getOrDefault(
                "private-key",
                Path.of(System.getProperty("user.home"), ".central-docs-licensing", "license-private-key.pem").toString()));

        LicenseDocument unsigned = new LicenseDocument(
                "CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                customer.trim(), machine.trim(), LocalDate.now().toString(), "");
        String signature = LicenseCryptography.sign(unsigned, PemKeys.privateKey(Files.readAllBytes(privateKey)));
        LicenseDocument signed = new LicenseDocument(
                unsigned.licenseId(), unsigned.customer(), unsigned.machineCode(), unsigned.issuedAt(), signature);
        Files.createDirectories(output.toAbsolutePath().getParent());
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(output.toFile(), signed);
        System.out.println("Licencia creada: " + output.toAbsolutePath());
        System.out.println("ID: " + signed.licenseId());
        System.out.println("Cliente: " + signed.customer());
        System.out.println("Equipo: " + signed.machineCode());
    }

    private static HashMap<String, String> options(String[] args) {
        HashMap<String, String> values = new HashMap<>();
        for (int index = 0; index < args.length - 1; index += 2) {
            values.put(args[index].replaceFirst("^--", ""), args[index + 1]);
        }
        return values;
    }

    private static String required(HashMap<String, String> options, String name) {
        String value = options.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Falta --" + name);
        }
        return value;
    }
}
