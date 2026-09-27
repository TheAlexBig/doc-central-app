package com.big.dreamer.doccentral.document.carsale.template;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarSaleTemplateRepositoryTests {
    @TempDir Path directory;

    @Test
    void initializesTheResourceCatalogAndExposesItsMetadata() {
        CarSaleTemplateRepository repository = repository();
        repository.initializeTemplates();

        assertThat(repository.list()).hasSize(9)
                .allSatisfy(template -> {
                    assertThat(template.version()).isEqualTo(2);
                    assertThat(template.section()).isNotBlank();
                    assertThat(template.content()).isNotBlank();
                });
        assertThat(repository.findAll().get("people-document.txt"))
                .contains(":sellerName", ":buyerName")
                .doesNotContain(":givenName");
    }

    @Test
    void validatesExactVariablesAndCanResetTemplate() {
        CarSaleTemplateRepository repository = repository();
        repository.initializeTemplates();
        String original = repository.findAll().get("document.txt");

        assertThatThrownBy(() -> repository.save("document.txt", original.replace(":price", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(":price");
        assertThatThrownBy(() -> repository.save("document.txt", original + " :invented"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(":invented");

        repository.save("document.txt", original + " Texto personalizado.");
        assertThat(repository.reset("document.txt").usingDefault()).isTrue();
    }

    private CarSaleTemplateRepository repository() {
        ApplicationDirectories directories = new ApplicationDirectories(
                directory.resolve("data").toString(), directory.resolve("documents").toString());
        directories.initialize();
        return new CarSaleTemplateRepository(directories);
    }
}
