# Política de seguridad

## Versiones compatibles

Solo la versión estable más reciente de Central Docs recibe correcciones de
seguridad. Los usuarios deben actualizar a la última versión publicada antes
de reportar un problema ya corregido.

## Reportar una vulnerabilidad

No publique vulnerabilidades, datos personales, documentos legales reales ni
credenciales en un issue público.

Utilice la opción **Report a vulnerability** de la pestaña **Security** del
repositorio `TheAlexBig/doc-central-app`. El reporte debe incluir, cuando sea
posible:

- versión afectada;
- descripción e impacto;
- pasos mínimos para reproducir el problema;
- archivos o rutas involucradas sin incluir información personal real;
- mitigación sugerida, si existe.

El mantenedor `TheAlexBig` es responsable de recibir, revisar y coordinar los
reportes. Se intentará confirmar la recepción dentro de siete días y comunicar
el estado o una corrección dentro de treinta días. Los tiempos pueden variar
según la complejidad y disponibilidad del mantenedor.

## Alcance

Son especialmente relevantes:

- acceso no autorizado a datos o documentos locales;
- exposición del servidor local fuera de `127.0.0.1`;
- escritura o lectura fuera de las carpetas previstas;
- manipulación del proceso de actualización, construcción o firma;
- dependencias vulnerables explotables en la aplicación instalada;
- generación de documentos con contenido no solicitado o ejecutable.

Problemas de redacción jurídica, solicitudes de funciones y errores sin
impacto de seguridad deben presentarse como issues normales sin datos
personales.

## Divulgación coordinada

Solicitamos mantener el reporte privado hasta que exista una mitigación y los
usuarios tengan tiempo razonable para actualizar. El mantenedor reconocerá al
reportante si este lo desea y si hacerlo no crea riesgos adicionales.
