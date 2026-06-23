package com.big.dreamer.doccentral.api;

import com.big.dreamer.doccentral.agent.service.AgentStorageException;
import com.big.dreamer.doccentral.document.carsale.service.DocumentGenerationException;
import com.big.dreamer.doccentral.document.history.service.DocumentHistoryStorageException;
import com.big.dreamer.doccentral.person.service.PersonStorageException;
import com.big.dreamer.doccentral.vehicle.service.VehicleStorageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Map<String, String> REQUEST_FIELD_NAMES = Map.ofEntries(
            Map.entry("seller", "vendedor"),
            Map.entry("buyer", "comprador"),
            Map.entry("vehicle", "vehiculo"),
            Map.entry("document", "documento"),
            Map.entry("legalAgent", "agente_juridico"),
            Map.entry("givenName", "nombre"),
            Map.entry("lastName", "apellido"),
            Map.entry("state", "departamento"),
            Map.entry("settlement", "domicilio"),
            Map.entry("gender", "genero"),
            Map.entry("age", "edad"),
            Map.entry("job", "oficio"),
            Map.entry("licensePlate", "placa"),
            Map.entry("brand", "marca"),
            Map.entry("model", "modelo"),
            Map.entry("factoryYear", "fabricado"),
            Map.entry("capacity", "capacidad"),
            Map.entry("domain", "dominio"),
            Map.entry("vehicleClass", "clase"),
            Map.entry("engineNumber", "num_motor"),
            Map.entry("chassisNumber", "num_chasis"),
            Map.entry("vinNumber", "num_vin"),
            Map.entry("garment", "calidad_de"),
            Map.entry("institution", "institucion"),
            Map.entry("price", "precio"),
            Map.entry("signDate", "fecha_firma"),
            Map.entry("signHour", "hora_firma"),
            Map.entry("identifiesSeller", "identifica_vendedor"),
            Map.entry("identifiesBuyer", "identifica_comprador"));

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fields.putIfAbsent(publicFieldName(error.getField()), error.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, "Request validation failed.", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest() {
        return response(HttpStatus.BAD_REQUEST, "Request body is invalid.", Map.of());
    }

    @ExceptionHandler(DocumentGenerationException.class)
    public ResponseEntity<ApiError> handleGenerationFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "The document could not be generated.", Map.of());
    }

    @ExceptionHandler(DocumentHistoryStorageException.class)
    public ResponseEntity<ApiError> handleDocumentHistoryStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "The generated document history could not be accessed.", Map.of());
    }

    @ExceptionHandler(AgentStorageException.class)
    public ResponseEntity<ApiError> handleAgentStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "The locally saved agents could not be accessed.", Map.of());
    }

    @ExceptionHandler(PersonStorageException.class)
    public ResponseEntity<ApiError> handlePersonStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "The locally saved people could not be accessed.", Map.of());
    }

    @ExceptionHandler(VehicleStorageException.class)
    public ResponseEntity<ApiError> handleVehicleStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "The locally saved vehicles could not be accessed.", Map.of());
    }

    private ResponseEntity<ApiError> response(HttpStatus status, String message, Map<String, String> fields) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                fields));
    }

    private String publicFieldName(String fieldName) {
        String[] segments = fieldName.split("\\.");
        for (int index = 0; index < segments.length; index++) {
            segments[index] = REQUEST_FIELD_NAMES.getOrDefault(segments[index], segments[index]);
        }
        return String.join(".", segments);
    }
}
