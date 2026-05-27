package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class CarSaleDocumentServiceTests {

    private final CarSaleDocumentService service = new CarSaleDocumentService();

    @Test
    void createsVehicleSaleDocumentWithBothSignaturesAndCorrectGenderTitles() throws Exception {
        PersonDetails seller = new PersonDetails(
                "Maria", "Lopez", "San Salvador", "San Salvador", "00000000-0",
                "0000-000000-000-0", "Femenino", "35", "Abogada");
        PersonDetails buyer = new PersonDetails(
                "Carlos", "Perez", "La Libertad", "Santa Tecla", "11111111-1",
                "1111-111111-111-1", "Masculino", "30", "Ingeniero");
        var request = new CarSaleDocumentRequest(
                seller,
                buyer,
                new CarDetails("P-123", "Toyota", "Corolla", "Azul", "2020", "5",
                        "Propiedad", "Sedan", "MOTOR1", "CHASIS1", "VIN1"),
                new DocumentDetails("Propiedad", "", "DIEZ MIL", "Santa Tecla",
                        "La Libertad", "26 de mayo de 2026", "diez horas", "No", "Si"),
                new LegalAgentDetails("Ana", "Notaria", "La Libertad", "Santa Tecla", "Femenino"));

        byte[] bytes = service.createDocument(request);

        assertThat(bytes).isNotEmpty();
        try (XWPFDocument generated = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String text = generated.getParagraphs().stream()
                    .map(paragraph -> paragraph.getText())
                    .reduce("", (left, right) -> left + right);
            String tableText = generated.getTables().stream()
                    .flatMap(table -> table.getRows().stream())
                    .flatMap(row -> row.getTableCells().stream())
                    .map(cell -> cell.getText())
                    .reduce("", (left, right) -> left + right);

            assertThat(text)
                    .contains("LA VENDEDORA")
                    .contains("EL COMPRADOR")
                    .contains("NOTARIA")
                    .contains("a quien no conozco")
                    .contains("a quien hoy conozco");
            assertThat(tableText)
                    .contains("Carlos Perez")
                    .contains("Maria Lopez");
        }
    }
}
