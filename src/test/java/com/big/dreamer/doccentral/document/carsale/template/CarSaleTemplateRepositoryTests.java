package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
                CarSaleTemplates.PREVIOUS_CURRENT_CAR_DOCUMENT,
                StandardCharsets.UTF_8);
        Files.writeString(
                directories.templatesDirectory().resolve("people-authentic.txt"),
                CarSaleTemplates.PREVIOUS_PEOPLE_AUTHENTIC,
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
                .contains(":sellerRole")
                .contains("TIPO: :vehicleType")
                .doesNotContain("DOMINIO AJENO");
        assertThat(repository.load().peopleAuthentic())
                .isEqualTo(CarSaleTemplates.PEOPLE_AUTHENTIC)
                .contains("carácter personal")
                .contains("a quien en adelante denominaré");
        assertThat(repository.load().document()).isEqualTo("Plantilla personalizada");
        assertThat(repository.load().documentAuthentic())
                .isEqualTo(CarSaleTemplates.DOCUMENT_AUTHENTIC)
                .contains("plazo de quince días")
                .contains("jurisdicción del distrito");
        assertThat(repository.load().legalAuthentic())
                .isEqualTo(CarSaleTemplates.LEGAL_AUTHENTIC)
                .contains("En el distrito de")
                .doesNotContain("En la ciudad de");
    }

    @Test
    void migratesFirstSectionVariantWithoutSertracenDeadline() throws Exception {
        ApplicationDirectories directories = new ApplicationDirectories(
                temporaryDirectory.resolve("variant-data").toString(),
                temporaryDirectory.resolve("variant-documents").toString());
        directories.initialize();
        Files.createDirectories(directories.templatesDirectory());
        Files.writeString(
                directories.templatesDirectory().resolve("document.txt"),
                CarSaleTemplates.PREVIOUS_DOCUMENT_WITHOUT_DEADLINE,
                StandardCharsets.UTF_8);

        CarSaleTemplateRepository repository = new CarSaleTemplateRepository(directories);
        repository.initializeTemplates();

        assertThat(repository.load().document())
                .isEqualTo(CarSaleTemplates.DOCUMENT)
                .contains("jurisdicción del distrito de :settlement")
                .doesNotContain("esta ciudad")
                .doesNotContain("quince días");
    }

    @Test
    void validatesRequiredVariablesAndCanResetTemplate() {
        ApplicationDirectories directories = new ApplicationDirectories(
                temporaryDirectory.resolve("editable-data").toString(),
                temporaryDirectory.resolve("editable-documents").toString());
        directories.initialize();
        CarSaleTemplateRepository repository = new CarSaleTemplateRepository(directories);
        repository.initializeTemplates();

        assertThatThrownBy(() -> repository.save("car-document.txt", "Texto sin variables"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(":licensePlate");
        String customized = CarSaleTemplates.CAR_DOCUMENT + " Texto personalizado.";
        assertThat(repository.save("car-document.txt", customized)).isEqualTo(customized);
        assertThat(repository.reset("car-document.txt")).isEqualTo(CarSaleTemplates.CAR_DOCUMENT);
    }
}
