package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import com.big.dreamer.doccentral.document.carsale.template.CarSaleTemplateRepository;
import com.big.dreamer.doccentral.storage.ApplicationDirectories;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class CarSaleDocumentServiceTests {

    @TempDir
    Path temporaryDirectory;

    private CarSaleDocumentService service;

    @BeforeEach
    void setUp() {
        ApplicationDirectories directories = new ApplicationDirectories(
                temporaryDirectory.resolve("data").toString(),
                temporaryDirectory.resolve("documents").toString());
        directories.initialize();
        CarSaleTemplateRepository templateRepository = new CarSaleTemplateRepository(directories);
        templateRepository.initializeTemplates();
        service = new CarSaleDocumentService(templateRepository);
    }

    @Test
    void createsVehicleSaleDocumentWithBothSignaturesAndCorrectGenderTitles() throws Exception {
        PersonDetails seller = new PersonDetails(
                "  Maria  ", "  De   Lopez  ", "San Salvador", "San Salvador", "00000000-0",
                "Femenino", "35", "Abogada");
        PersonDetails buyer = new PersonDetails(
                "  Carla ", " Perez  ", "La Libertad", "Santa Tecla", "11111111-1",
                "Femenino", "30", "Ingeniera");
        var request = new CarSaleDocumentRequest(
                seller,
                buyer,
                new CarDetails("P-123", "Toyota", "Corolla", "Azul", "2020", "CINCO ASS",
                        "Propiedad", "Automóvil", "Camioneta", null, null, null, null, null,
                        "CUATRO x CUATRO", "MOTOR1", "CHASIS1", "VIN1"),
                new DocumentDetails("Propiedad", "", "CINCO MIL CON VEINTIDÓS CENTAVOS", "Santa Tecla",
                        "La Libertad", "26 de mayo de 2026", "diez horas", "No", "Si"),
            new LegalAgentDetails(" Ana  Maria ", " Notaria ", "La Libertad", "Santa Tecla", "Femenino", "Notario"));

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
                    .contains("LA COMPRADORA")
                    .contains("NOTARIA")
                    .contains("MARIA DE LOPEZ")
                    .contains("CARLA PEREZ")
                    .contains("ANA MARIA NOTARIA")
                    .doesNotContain("  MARIA")
                    .doesNotContain("DE   LOPEZ")
                    .doesNotContain("Carla Perez")
                    .doesNotContain("ANA  MARIA")
                    .contains("la vendedora es dueña y actual poseedora")
                    .contains("la vendedora vende a la compradora")
                    .contains("la compradora acepta")
                    .contains("dándose por recibida")
                    .contains("le entrega la vendedora")
                    .contains("ambas contratantes")
                    .contains("las comparecientes")
                    .contains("en el distrito de Santa Tecla")
                    .containsOnlyOnce("SERTRACEN dentro del plazo de quince días")
                    .contains("y en adelante me denominaré “LA VENDEDORA”")
                    .contains("y en adelante me denominaré “LA COMPRADORA”")
                    .contains("quien actúa en su carácter personal y a quien en adelante denominaré “LA VENDEDORA”")
                    .contains("quien actúa en su carácter personal y a quien en adelante denominaré “LA COMPRADORA”")
                    .contains("se someten a la jurisdicción del distrito de Santa Tecla")
                    .contains("EN CALIDAD DE Propiedad. II)")
                    .contains("CAPACIDAD: CINCO ASS")
                    .contains("DOMINIO: Propiedad")
                    .contains("CLASE: Automóvil")
                    .contains("TIPO: Camioneta")
                    .contains("TRACCIÓN: CUATRO x CUATRO")
                    .contains("CINCO MIL DÓLARES CON VEINTIDÓS CENTAVOS DE DÓLAR DE LOS ESTADOS UNIDOS DE AMÉRICA")
                    .doesNotContain("CON VEINTIDÓS CENTAVOS DÓLARES")
                    .doesNotContain("ESTADOS UNIDOPS")
                    .doesNotContain("DOMINIO AJENO")
                    .doesNotContain(",.")
                    .doesNotContain("el comprador")
                    .doesNotContain("el vendedor")
                    .doesNotContain("dándose por recibido")
                    .doesNotContain("en la ciudad de")
                    .doesNotContain("domicilio especial el de esta ciudad")
                    .contains("a quien no conozco")
                    .contains("a quien hoy conozco")
                    .doesNotContain("Numero Identificación Tributaria")
                    .doesNotContain(":nit");
            assertThat(tableText)
                    .contains("CARLA PEREZ")
                    .contains("MARIA DE LOPEZ");
        }
    }
}
