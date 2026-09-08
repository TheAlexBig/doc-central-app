package com.big.dreamer.doccentral.document.marriage.service;

import com.big.dreamer.doccentral.document.marriage.model.MarriageDocumentRequest;
import com.big.dreamer.doccentral.document.marriage.model.RecognizedChild;
import com.big.dreamer.doccentral.document.marriage.template.MarriageTemplateRepository;
import com.big.dreamer.doccentral.document.render.LegalDocumentRenderer;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static com.big.dreamer.doccentral.document.marriage.service.MarriageRulesServiceTests.*;
import static org.assertj.core.api.Assertions.assertThat;

class MarriageDocumentServiceTests {
    @TempDir Path directory;

    @Test
    void generatesPremaritalActMarriageInstrumentAndRegistrationControlInWordAndPdf() {
        MarriageTemplateRepository templates = new MarriageTemplateRepository(new ApplicationDirectories(
                directory.resolve("data").toString(), directory.resolve("documents").toString()));
        templates.initializeTemplates();
        MarriageDocumentService service = new MarriageDocumentService(
                templates, new MarriageRulesService(), new LegalDocumentRenderer());
        MarriageDocumentRequest request = request(
                party("Femenino", "SINGLE", LocalDate.of(1990, 1, 1)),
                party("Masculino", "SINGLE", LocalDate.of(1990, 1, 1)),
                witnesses(), details(null), List.of(
                        new RecognizedChild("HIJO UNO", "PARTIDA UNO"),
                        new RecognizedChild("HIJO DOS", "PARTIDA DOS")));

        assertThat(service.assemble(request)).extracting(section -> section.text())
                .anySatisfy(text -> assertThat(text).contains(
                        "ACTA PREMATRIMONIAL", "COMUNIDAD DIFERIDA", "HIJO UNO", "HIJO DOS"))
                .anySatisfy(text -> assertThat(text).contains(
                        "NÚMERO DOCE", "SÍ, QUIERO", "TESTIGO UNO", "HIJO UNO", "HIJO DOS"))
                .anySatisfy(text -> assertThat(text).contains("HOJA DE CONTROL", "quince días hábiles"));
        assertThat(service.assemble(request).getFirst().signatures())
                .extracting(signature -> signature.title())
                .contains("NOTARIO AUTORIZANTE");
        assertThat(service.createDocument(request)).isNotEmpty();
        assertThat(service.createPdfDocument(request)).isNotEmpty();
    }
}
