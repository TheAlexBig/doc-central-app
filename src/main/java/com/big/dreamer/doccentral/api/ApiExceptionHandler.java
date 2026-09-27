package com.big.dreamer.doccentral.api;

import com.big.dreamer.doccentral.agent.service.AgentStorageException;
import com.big.dreamer.doccentral.document.DocumentGenerationException;
import com.big.dreamer.doccentral.document.carsale.service.CarSaleRequestValidationException;
import com.big.dreamer.doccentral.document.history.service.DocumentHistoryStorageException;
import com.big.dreamer.doccentral.person.service.PersonStorageException;
import com.big.dreamer.doccentral.license.service.LicenseException;
import com.big.dreamer.doccentral.vehicle.service.VehicleStorageException;
import com.big.dreamer.doccentral.storage.StorageBackupException;
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
            Map.entry("debtor", "deudor"),
            Map.entry("creditor", "acreedor"),
            Map.entry("guarantor", "fiador"),
            Map.entry("pledgedVehicle", "vehiculo_garantia"),
            Map.entry("terms", "condiciones"),
            Map.entry("partyOne", "contrayente_uno"),
            Map.entry("partyTwo", "contrayente_dos"),
            Map.entry("witnesses", "testigos"),
            Map.entry("recognizedChildren", "hijos_reconocidos"),
            Map.entry("details", "celebracion"),
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
            Map.entry("vehicleType", "tipo"),
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
        return response(HttpStatus.BAD_REQUEST, "La solicitud contiene campos inválidos.", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest() {
        return response(HttpStatus.BAD_REQUEST, "El contenido de la solicitud no es válido.", Map.of());
    }

    @ExceptionHandler(DocumentGenerationException.class)
    public ResponseEntity<ApiError> handleGenerationFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo generar el documento.", Map.of());
    }

    @ExceptionHandler(CarSaleRequestValidationException.class)
    public ResponseEntity<ApiError> handleCarSaleValidation(CarSaleRequestValidationException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), exception.fields());
    }

    @ExceptionHandler(LicenseException.class)
    public ResponseEntity<ApiError> handleLicenseFailure(LicenseException exception) {
        return response(HttpStatus.FORBIDDEN, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DocumentHistoryStorageException.class)
    public ResponseEntity<ApiError> handleDocumentHistoryStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo acceder al historial de documentos generados.", Map.of());
    }

    @ExceptionHandler(AgentStorageException.class)
    public ResponseEntity<ApiError> handleAgentStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo acceder a los notarios guardados localmente.", Map.of());
    }

    @ExceptionHandler(PersonStorageException.class)
    public ResponseEntity<ApiError> handlePersonStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo acceder a las personas guardadas localmente.", Map.of());
    }

    @ExceptionHandler(VehicleStorageException.class)
    public ResponseEntity<ApiError> handleVehicleStorageFailure() {
        return response(HttpStatus.INTERNAL_SERVER_ERROR,
                "No se pudo acceder a los vehículos guardados localmente.", Map.of());
    }

    @ExceptionHandler(StorageBackupException.class)
    public ResponseEntity<ApiError> handleBackupFailure(StorageBackupException exception) {
        return response(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
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
