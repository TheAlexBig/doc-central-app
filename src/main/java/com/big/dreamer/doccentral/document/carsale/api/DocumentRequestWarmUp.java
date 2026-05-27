package com.big.dreamer.doccentral.document.carsale.api;

import com.big.dreamer.doccentral.document.carsale.model.CarSaleDocumentRequest;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class DocumentRequestWarmUp {

    private static final String SAMPLE_REQUEST = """
            {
              "vendedor": {"nombre":"Inicial","apellido":"Vendedor","departamento":"Departamento",
                "domicilio":"Municipio","documento":"00000000-0","nit":"0000-000000-000-0",
                "genero":"Masculino","edad":"30","oficio":"Oficio"},
              "comprador": {"nombre":"Inicial","apellido":"Comprador","departamento":"Departamento",
                "domicilio":"Municipio","documento":"00000000-0","nit":"0000-000000-000-0",
                "genero":"Masculino","edad":"30","oficio":"Oficio"},
              "vehiculo": {"placa":"P-000","marca":"Marca","modelo":"Modelo","color":"Color",
                "fabricado":"2026","capacidad":"5","dominio":"Propiedad","clase":"Clase",
                "num_motor":"MOTOR","num_chasis":"CHASIS","num_vin":"VIN"},
              "documento": {"calidad_de":"Propiedad","institucion":"","precio":"PRECIO",
                "domicilio":"Municipio","departamento":"Departamento","fecha_firma":"FECHA",
                "hora_firma":"HORA","identifica_vendedor":"No","identifica_comprador":"No"},
              "agente_juridico": {"nombre":"Inicial","apellido":"Notario","departamento":"Departamento",
                "domicilio":"Municipio","genero":"Masculino"}
            }
            """;

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public DocumentRequestWarmUp(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @PostConstruct
    void initializeRequestProcessing() {
        try {
            CarSaleDocumentRequest request = objectMapper.readValue(SAMPLE_REQUEST, CarSaleDocumentRequest.class);
            validator.validate(request);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize document request processing.", exception);
        }
    }
}
