# Central Docs Desktop Application

Java 21 and Spring Boot 4 local application for generating legal document
templates as Word files. The packaged Windows application embeds the React
interface and runs entirely offline on the user's computer.

## Development

```bash
mvn spring-boot:run
```

The server binds only to `127.0.0.1`. During React development, Vite proxies
`/api` requests to the backend at `http://127.0.0.1:8080`. Set
`DOC_WEB_ORIGINS` only when a different local development origin is needed.

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
Generated Word files are returned as downloads and are also saved locally.

## Offline Desktop Behavior

The installed application starts a local Spring Boot server on
`http://127.0.0.1:17831`, serves the compiled React application from that
process, and opens it in the user's default browser. API requests remain local
under `/api`. A second application launch checks the running Central Docs
instance and opens its browser interface instead of starting another server.

No network service, database, Docker installation, or separately installed
Java runtime is needed after the MSI is installed. Default templates are
created locally on first launch and remain editable offline:

| Content | Windows location |
| --- | --- |
| Editable templates | `%LOCALAPPDATA%\Central Docs\templates\car-sale\` |
| Optional settings | `%LOCALAPPDATA%\Central Docs\config\application.yml` |
| Generated documents | `%USERPROFILE%\Documents\Central Docs\Documents\` |

The template placeholders must remain present where the associated form values
should be inserted. The generated local settings file contains an example for
changing the document output directory.

## Windows Installer

Build on Windows with a Java 21 JDK that supplies `jpackage`, Node.js/npm, and
WiX Toolset available on `PATH`. Place `doc-central-app` and
`doc-central-forms` beside one another, then run from this repository:

```powershell
.\packaging\windows\build-installer.ps1 -Version 1.0.0
```

The script builds React, embeds its production files in the Spring Boot jar,
generates the application icon, and creates an MSI under `target\installer`.
The MSI bundles its Java runtime and supplies a desktop shortcut, Start Menu
entry, stable upgrade identity, normal Windows upgrade behavior, and uninstall
support.
Increment the installer version for upgrades and keep the Maven project
version aligned for published application releases.
