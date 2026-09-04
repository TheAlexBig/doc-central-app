# Política de privacidad de Central Docs

Última actualización: 4 de septiembre de 2026.

Central Docs es una aplicación de escritorio que funciona localmente. La
aplicación no incluye telemetría, publicidad, analítica, cuentas en línea ni
servicios de sincronización, y no transfiere información a otros sistemas de
red salvo que el usuario lo solicite expresamente.

## Información tratada

Central Docs puede tratar y conservar en el equipo información introducida por
el usuario, incluyendo:

- nombres, DUI, fecha de nacimiento, domicilio, género y profesión u oficio;
- información de compradores, vendedores, deudores, acreedores, preparadores y
  agentes jurídicos;
- placas, marcas, modelos, VIN, motor, chasis y otras características de
  vehículos;
- condiciones de compraventa y mutuo, incluidos montos, plazos, intereses,
  bancos, números de cuenta, garantías y jurisdicción;
- borradores e historial de documentos;
- documentos Word y PDF generados;
- preferencias, plantillas y configuración local;
- logs técnicos de funcionamiento y fallos de inicio.

## Almacenamiento local

De manera predeterminada, los datos de aplicación se guardan en:

```text
%LOCALAPPDATA%\Central Docs\
```

Esto incluye archivos JSON de personas, vehículos, agentes e historial,
plantillas editables, configuración, respaldos automáticos y logs técnicos.
Los documentos generados se guardan normalmente en:

```text
%USERPROFILE%\Documents\Central Docs\Documents\
```

El usuario puede configurar otra ubicación para los documentos. La aplicación
abre una interfaz local mediante `http://127.0.0.1:17831`; esa comunicación no
sale del equipo.

## Transmisión y terceros

Central Docs no envía los datos anteriores al mantenedor, a SignPath, a GitHub
ni a servicios de terceros. Descargar el instalador, visitar GitHub o enviar
voluntariamente un reporte de seguridad ocurre fuera de la aplicación y está
sujeto a las políticas del servicio utilizado.

## Control y eliminación

El usuario controla los archivos locales y puede eliminar datos guardados
desde la aplicación cuando esa opción esté disponible. Para borrar todos los
datos después de desinstalar, debe eliminar manualmente las carpetas indicadas
arriba. La desinstalación conserva deliberadamente los documentos y datos del
usuario para evitar pérdida accidental.

Antes de borrar información, se recomienda conservar una copia de cualquier
documento legal necesario.

## Seguridad y responsabilidades

Central Docs no cifra por sí mismo los archivos locales. La protección del
equipo, la cuenta de Windows, las copias de seguridad y los permisos de las
carpetas corresponde al usuario o al administrador del sistema. No se deben
introducir datos personales en equipos compartidos o no confiables.

La aplicación ayuda a preparar documentos, pero no sustituye la revisión de un
abogado o notario ni garantiza la vigencia jurídica de una plantilla.

## Cambios a esta política

Los cambios materiales se publicarán en el repositorio y en las notas de la
versión correspondiente. Cualquier función futura que transmita datos deberá
documentarse y requerir una acción o consentimiento explícito del usuario.

## Contacto

Las preguntas generales pueden presentarse en el repositorio de GitHub. Las
vulnerabilidades no deben publicarse como issues; deben enviarse mediante el
reporte privado descrito en [SECURITY.md](SECURITY.md).
