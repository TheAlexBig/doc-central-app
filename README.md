# Central Docs API

Java 21 and Spring Boot 4 API for generating legal document templates as Word
files. The implemented template is the Salvadoran vehicle purchase-and-sale
agreement with notarial authentication.

## Run Locally

```bash
mvn spring-boot:run
```

The frontend origin defaults to `http://localhost:3000`. Set
`DOC_WEB_ORIGINS` to a comma-separated origin list when deploying.

## Generate A Document

`POST /api/v1/documents/car-sale` returns a downloadable
`compra-venta.docx` file. The JSON request uses the form's Spanish domain
names:

```json
{
  "vendedor": {},
  "comprador": {},
  "vehiculo": {},
  "documento": {},
  "agente_juridico": {}
}
```

Missing required fields return HTTP `400` with a field-level validation map.
The legal template text is intentionally preserved from the original
implementation.
