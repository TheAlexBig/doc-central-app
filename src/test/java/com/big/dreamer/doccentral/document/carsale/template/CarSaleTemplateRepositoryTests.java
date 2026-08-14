package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CarSaleTemplateRepositoryTests {

    @TempDir
    Path temporaryDirectory;

    @Test
    void migratesReleasedTemplatesButPreservesCustomTemplates() throws Exception {
        ApplicationDirectories directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        Files.createDirectories(directories.templatesDirectory());
        Files.writeString(
                directories.templatesDirectory().resolve("car-document.txt"),
                CarSaleTemplates.RELEASED_CAR_DOCUMENT,
                StandardCharsets.UTF_8);
        Files.writeString(
                directories.templatesDirectory().resolve("document.txt"),
                "Plantilla personalizada",
                StandardCharsets.UTF_8);
        Files.writeString(
                directories.templatesDirectory().resolve("legal-authentic.txt"),
                CarSaleTemplates.RELEASED_LEGAL_AUTHENTIC,
                StandardCharsets.UTF_8);

        CarSaleTemplateRepository repository = new CarSaleTemplateRepository(directories);
        repository.initializeTemplates();

        assertThat(repository.load().carDocument())
                .isEqualTo(CarSaleTemplates.CAR_DOCUMENT)
                .contains(":sellerOrdinal")
                .contains("TIPO: :vehicleType")
                .doesNotContain("DOMINIO AJENO");
        assertThat(repository.load().document()).isEqualTo("Plantilla personalizada");
        assertThat(repository.load().legalAuthentic())
                .isEqualTo(CarSaleTemplates.LEGAL_AUTHENTIC)
                .contains("En el distrito de")
                .doesNotContain("En la ciudad de");
    }
}
