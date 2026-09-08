package com.big.dreamer.doccentral.document.mutual.template;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.assertj.core.api.Assertions.*;

class MutualTemplateRepositoryTests {
    @TempDir Path directory;

    @Test
    void persistsCustomBlocksAcrossRestartAndValidatesExactVariables() {
        ApplicationDirectories directories = new ApplicationDirectories(directory.resolve("data").toString(), directory.resolve("docs").toString());
        MutualTemplateRepository repository = new MutualTemplateRepository(directories);
        repository.initializeTemplates();
        String original = repository.findAll().get("principal.txt");
        assertThatThrownBy(() -> repository.save("principal.txt", original.replace(":amount", ":amountExtra")))
                .hasMessageContaining("Faltan variables obligatorias: :amount");
        assertThatThrownBy(() -> repository.save("principal.txt", original + " :unknown"))
                .hasMessageContaining("Variables desconocidas");
        assertThatThrownBy(() -> repository.save("../principal.txt", original)).hasMessageContaining("desconocida");
        assertThatThrownBy(() -> repository.save("principal.txt", " ")).hasMessageContaining("vacía");
        repository.save("principal.txt", original + " Personalizado.");
        MutualTemplateRepository restarted = new MutualTemplateRepository(directories);
        restarted.initializeTemplates();
        assertThat(restarted.findAll().get("principal.txt")).endsWith("Personalizado.");
        assertThat(restarted.reset("principal.txt").content()).isEqualTo(original);
        assertThat(restarted.list()).allMatch(template -> template.usingDefault());
    }

    @Test
    void substitutesExactTokensInOnePassWithoutInterpretingUserValues() {
        assertThat(EditableTemplateRepository.render(":debtor / :debtorRole", Map.of("debtor", "$Ana :debtorRole", "debtorRole", "LA DEUDORA")))
                .isEqualTo("$Ana :debtorRole / LA DEUDORA");
    }

    @Test
    void migratesKnownLegacyDefaultsWithoutOverwritingCustomTemplates() throws Exception {
        ApplicationDirectories directories = new ApplicationDirectories(
                directory.resolve("data").toString(), directory.resolve("docs").toString());
        Path templates = directories.templatesDirectory("mutual");
        Files.createDirectories(templates);
        Files.writeString(templates.resolve("contract.txt"),
                "NOSOTROS: :debtor, que en lo sucesivo me denominaré \":debtorRole\"; y :creditor, "
                        + "que en adelante me denominaré \":creditorRole\", por medio del presente instrumento "
                        + "OTORGAMOS un CONTRATO DE MUTUO SIMPLE, sujeto a las siguientes cláusulas: :clauses "
                        + "En :signingPlace, departamento de :signingState, a :signingDate.");
        Files.writeString(templates.resolve("interest.txt"),
                "III) INTERESES: La suma mutuada devengará :monthlyInterest por ciento de interés mensual y, "
                        + "en caso de mora, :defaultInterest por ciento mensual adicional, sin exceder la tasa "
                        + "máxima legal vigente.");
        Files.writeString(templates.resolve("payment-schedule.txt"),
                ":number PLAN DE PAGOS: Capital :capital; intereses :interest; total a pagar :total. "
                        + ":periodicity. Vencimientos: :schedule.");
        Files.writeString(templates.resolve("principal.txt"), "Plantilla personalizada :debtorSubject :fromCreditor :amount");

        MutualTemplateRepository repository = new MutualTemplateRepository(directories);
        repository.initializeTemplates();

        assertThat(repository.findAll().get("contract.txt")).contains(":mutualType");
        assertThat(repository.findAll().get("interest.txt")).contains(":interestTerms");
        assertThat(repository.findAll().get("payment-schedule.txt"))
                .contains("Los pagos se realizarán").doesNotContain("Vencimientos:");
        assertThat(repository.findAll().get("principal.txt")).startsWith("Plantilla personalizada");
    }
}
