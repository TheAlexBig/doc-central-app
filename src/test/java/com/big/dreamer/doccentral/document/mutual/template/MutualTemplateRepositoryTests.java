package com.big.dreamer.doccentral.document.mutual.template;

import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import com.big.dreamer.doccentral.document.template.EditableTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
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
}
