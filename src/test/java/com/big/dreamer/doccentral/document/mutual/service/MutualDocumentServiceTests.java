package com.big.dreamer.doccentral.document.mutual.service;

import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.mutual.model.MutualDocumentRequest;
import com.big.dreamer.doccentral.document.mutual.model.MutualTerms;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MutualDocumentServiceTests {

    private final MutualDocumentService service = new MutualDocumentService();

    @Test
    void createsMutualWithOptionalInterestAndGuaranteeClauses() throws Exception {
        MutualDocumentRequest request = request(true);

        byte[] bytes = service.createDocument(request);

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String text = document.getParagraphs().stream()
                    .map(paragraph -> paragraph.getText())
                    .reduce("", (left, right) -> left + right);
            assertThat(text)
                    .contains("CONTRATO DE MUTUO SIMPLE")
                    .contains("me denominaré \"LA DEUDORA\"")
                    .contains("me denominaré \"EL ACREEDOR\"")
                    .contains("INTERESES")
                    .contains("GARANTÍA")
                    .contains("letra de cambio")
                    .contains("SETECIENTOS CINCUENTA DÓLARES CON VEINTIDÓS CENTAVOS DE DÓLAR")
                    .contains("LA DEUDORA")
                    .contains("EL ACREEDOR");
        }
    }

    @Test
    void rejectsSamePersonAsDebtorAndCreditor() {
        MutualDocumentRequest valid = request(false);
        MutualDocumentRequest invalid = new MutualDocumentRequest(
                valid.debtor(), valid.debtor(), valid.terms(), valid.legalAgent());

        assertThatThrownBy(() -> MutualRequestValidator.validate(invalid))
                .hasMessageContaining("personas diferentes");
    }

    @Test
    void createsMutualAsPdf() {
        assertThat(service.createPdfDocument(request(true))).isNotEmpty();
    }

    private MutualDocumentRequest request(boolean guarantee) {
        return new MutualDocumentRequest(
                new PersonDetails("Jeimmi Ubeth", "Calderón Valle", "La Libertad",
                        "Antiguo Cuscatlán, Municipio de La Libertad Este", "CERO TRES-UNO",
                        "Femenino", "CUARENTA Y CINCO", "Estudiante"),
                new PersonDetails("Luis Arístides", "Díaz Madrid", "La Libertad",
                        "Santa Tecla, Municipio de La Libertad Sur", "CERO CUATRO-DOS",
                        "Masculino", "TREINTA Y SEIS", "Empresario"),
                new MutualTerms("SETECIENTOS CINCUENTA CON VEINTIDÓS CENTAVOS", "SEIS MESES",
                        "NUEVE DE OCTUBRE DE DOS MIL VEINTISÉIS", "SEIS",
                        "CIENTO OCHENTA Y CINCO", "BANCO PROMÉRICA", "DOS CERO CERO",
                        "TRES PUNTO SETENTA Y CINCO", "UNO", "GASTOS PERSONALES",
                        guarantee, guarantee ? "VEINTITRÉS DE ENERO DE DOS MIL VEINTISIETE" : "",
                        "TRES", "Distrito de Santa Tecla", "Distrito de Santa Tecla",
                        "La Libertad", "NUEVE DE ABRIL DE DOS MIL VEINTISÉIS",
                        "NUEVE HORAS CON TREINTA MINUTOS", "No", "No"),
                new LegalAgentDetails("Ana Evelyn", "Valladares Parada", "San Salvador",
                        "Distrito de San Salvador", "Femenino", "Notario"));
    }
}
