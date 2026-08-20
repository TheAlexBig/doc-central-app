package com.big.dreamer.doccentral.license.service;

import com.big.dreamer.doccentral.license.model.LicenseDocument;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LicenseServiceTests {

    private static final String MACHINE_CODE = "CD-0123456789ABCDEF01234567";

    @TempDir
    Path temporaryDirectory;

    private KeyPair keyPair;
    private LicenseService service;
    private ApplicationDirectories directories;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();
        directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("Central Docs/Documents").toString());
        directories.initialize();
        MachineCodeProvider machineCodeProvider = new MachineCodeProvider() {
            @Override
            public String machineCode() {
                return MACHINE_CODE;
            }
        };
        service = new LicenseService(
                directories, machineCodeProvider, new ObjectMapper(), keyPair.getPublic());
    }

    @Test
    void activatesSignedLicenseAndWritesOperationalAndReceiptCopies() {
        var license = signedLicense(MACHINE_CODE, "Cliente de prueba");

        var status = service.activate(license);

        assertThat(status.active()).isTrue();
        assertThat(status.customer()).isEqualTo("Cliente de prueba");
        assertThat(Files.exists(directories.licenseFile())).isTrue();
        assertThat(Files.exists(directories.licenseReceiptFile())).isTrue();
        service.requireActive();
    }

    @Test
    void rejectsLicenseForAnotherMachineOrModifiedCustomer() {
        assertThatThrownBy(() -> service.activate(signedLicense(
                "CD-FFFFFFFFFFFFFFFFFFFFFFFF", "Otro equipo")))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("otro equipo");

        LicenseDocument signed = signedLicense(MACHINE_CODE, "Cliente original");
        LicenseDocument modified = new LicenseDocument(
                signed.licenseId(), "Cliente modificado", signed.machineCode(),
                signed.issuedAt(), signed.signature());
        assertThatThrownBy(() -> service.activate(modified))
                .isInstanceOf(LicenseException.class)
                .hasMessageContaining("firma");
    }

    private LicenseDocument signedLicense(String machineCode, String customer) {
        LicenseDocument unsigned = new LicenseDocument(
                "CD-TEST0001", customer, machineCode, "2026-08-20", "");
        return new LicenseDocument(
                unsigned.licenseId(), unsigned.customer(), unsigned.machineCode(), unsigned.issuedAt(),
                LicenseCryptography.sign(unsigned, keyPair.getPrivate()));
    }
}
