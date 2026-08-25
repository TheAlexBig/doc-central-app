package com.big.dreamer.doccentral.document.carsale.service;

import com.big.dreamer.doccentral.document.carsale.model.CarDetails;
import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import com.big.dreamer.doccentral.document.carsale.model.DocumentDetails;
import com.big.dreamer.doccentral.document.carsale.model.LegalAgentDetails;
import com.big.dreamer.doccentral.document.carsale.model.PersonDetails;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CarSaleRequestValidatorTests {

    @Test
    void acceptsDifferentPartiesAuthenticatedByNotary() {
        assertThatCode(() -> CarSaleRequestValidator.validate(request("87654321-0", "Notario")))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsLawyerAsAuthenticatingAgent() {
        assertThatThrownBy(() -> CarSaleRequestValidator.validate(request("87654321-0", "Abogado")))
                .isInstanceOf(CarSaleRequestValidationException.class)
                .hasMessageContaining("notario");
    }

    @Test
    void rejectsSameDuiWithDifferentFormatting() {
        assertThatThrownBy(() -> CarSaleRequestValidator.validate(request("1234 5678 9", "Notario")))
                .isInstanceOf(CarSaleRequestValidationException.class)
                .hasMessageContaining("diferentes");
    }

    private CarSaleDocumentRequest request(String buyerDui, String role) {
        PersonDetails seller = new PersonDetails(
                "Maria", "Lopez", "San Salvador", "San Salvador",
                "12345678-9", "Femenino", "TREINTA", "Comerciante");
        PersonDetails buyer = new PersonDetails(
                "Carlos", "Perez", "La Libertad", "Santa Tecla",
                buyerDui, "Masculino", "TREINTA", "Ingeniero");
        return new CarSaleDocumentRequest(
                seller,
                buyer,
                new CarDetails(
                        "P-123", "Toyota", "Corolla", "Azul", "DOS MIL VEINTE",
                        "CINCO ASS", "Propiedad", "Automóvil", "Sedán", "M1", "C1", "V1"),
                new DocumentDetails(
                        "Propiedad", "", "DIEZ MIL", "Santa Tecla", "La Libertad",
                        "UNO DE ENERO", "DIEZ HORAS", "No", "No"),
                new LegalAgentDetails(
                        "Ana", "Garcia", "La Libertad", "Santa Tecla", "Femenino", role));
    }
}
