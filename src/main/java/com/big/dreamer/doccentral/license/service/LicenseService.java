package com.big.dreamer.doccentral.license.service;

import com.big.dreamer.doccentral.license.model.LicenseDocument;
import com.big.dreamer.doccentral.license.model.LicenseStatus;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import tools.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.PublicKey;

@Service
public class LicenseService {

    private final ApplicationDirectories directories;
    private final MachineCodeProvider machineCodeProvider;
    private final ObjectMapper objectMapper;
    private final PublicKey publicKey;

    @Autowired
    public LicenseService(
            ApplicationDirectories directories,
            MachineCodeProvider machineCodeProvider,
            ObjectMapper objectMapper) {
        this.directories = directories;
        this.machineCodeProvider = machineCodeProvider;
        this.objectMapper = objectMapper;
        try {
            this.publicKey = PemKeys.publicKey(
                    new ClassPathResource("license-public-key.pem").getInputStream().readAllBytes());
        } catch (Exception exception) {
            throw new LicenseException("No se pudo cargar la clave pública de licencias.", exception);
        }
    }

    LicenseService(
            ApplicationDirectories directories,
            MachineCodeProvider machineCodeProvider,
            ObjectMapper objectMapper,
            PublicKey publicKey) {
        this.directories = directories;
        this.machineCodeProvider = machineCodeProvider;
        this.objectMapper = objectMapper;
        this.publicKey = publicKey;
    }

    public LicenseStatus status() {
        String machineCode = machineCodeProvider.machineCode();
        if (Files.notExists(directories.licenseFile())) {
            return new LicenseStatus(false, machineCode, null, null, "Central Docs requiere activación.");
        }
        try {
            LicenseDocument license = objectMapper.readValue(directories.licenseFile().toFile(), LicenseDocument.class);
            validate(license, machineCode);
            return new LicenseStatus(true, machineCode, license.licenseId(), license.customer(), "Licencia activa.");
        } catch (Exception exception) {
            return new LicenseStatus(false, machineCode, null, null, "La licencia guardada no es válida para este equipo.");
        }
    }

    public LicenseStatus activate(LicenseDocument license) {
        String machineCode = machineCodeProvider.machineCode();
        validate(license, machineCode);
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(license) + System.lineSeparator();
            Files.createDirectories(directories.licenseFile().getParent());
            Files.writeString(directories.licenseFile(), json, StandardCharsets.UTF_8);
            Files.createDirectories(directories.licenseReceiptFile().getParent());
            Files.writeString(directories.licenseReceiptFile(), json, StandardCharsets.UTF_8);
            return status();
        } catch (Exception exception) {
            throw new LicenseException("No se pudo guardar la licencia.", exception);
        }
    }

    public void requireActive() {
        if (!status().active()) {
            throw new LicenseException("Central Docs requiere una licencia válida para generar documentos.");
        }
    }

    private void validate(LicenseDocument license, String machineCode) {
        if (license == null
                || blank(license.licenseId())
                || blank(license.customer())
                || blank(license.machineCode())
                || blank(license.issuedAt())
                || blank(license.signature())) {
            throw new LicenseException("El archivo de licencia está incompleto.");
        }
        if (!machineCode.equalsIgnoreCase(license.machineCode())) {
            throw new LicenseException("La licencia pertenece a otro equipo.");
        }
        if (!LicenseCryptography.verify(license, publicKey)) {
            throw new LicenseException("La firma de la licencia no es válida.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
