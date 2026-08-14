# Code signing policy

Free code signing provided by SignPath.io, certificate by SignPath Foundation.

## Alcance

Esta política cubre el MSI de Central Docs producido desde:

- `TheAlexBig/doc-central-app`, que contiene el host de escritorio, backend,
  plantillas y workflow del instalador;
- `TheAlexBig/doc-central-forms`, que contiene la interfaz React incorporada
  en el instalador.

Solo se solicitará la firma de versiones públicas de Central Docs construidas
por el workflow versionado del repositorio. No se firmarán artefactos locales,
software de terceros presentado como propio ni archivos creados fuera del
proceso autorizado.

## Responsables

| Rol | Responsable |
| --- | --- |
| Committer y autor | [`TheAlexBig`](https://github.com/TheAlexBig) |
| Revisor | [`TheAlexBig`](https://github.com/TheAlexBig) |
| Aprobador de firma | [`TheAlexBig`](https://github.com/TheAlexBig) |

Al existir actualmente un único mantenedor, una misma persona ocupa los tres
roles. Los cambios de colaboradores externos deben revisarse antes de
integrarse. Cada solicitud de firma requiere aprobación manual y autenticación
multifactor en GitHub y SignPath.

## Procedencia y controles

Una versión apta para firma debe:

1. provenir de revisiones públicas e identificables de ambos repositorios;
2. construirse exclusivamente en un runner alojado por GitHub;
3. superar las pruebas, lint y compilaciones configuradas;
4. producir primero un artifact de GitHub sin firma;
5. enviarse a SignPath mediante su integración de sistema de construcción
   confiable;
6. ser aprobada manualmente por el aprobador de firma;
7. verificar la firma Authenticode, sello de tiempo y editor esperado;
8. recibir una attestación de procedencia de GitHub después de ser firmada;
9. publicarse con su checksum SHA-256.

Los secretos y claves privadas de firma no se almacenan en los repositorios ni
se entregan al workflow. La configuración de artefactos de SignPath debe
restringir nombre, versión, metadata y estructura del MSI permitido.

## Versiones y revocación

Cada release firmado tendrá una etiqueta inmutable y notas que identifiquen
las revisiones utilizadas. Si se detecta que una versión firmada fue
comprometida, el mantenedor detendrá su distribución, investigará el incidente,
solicitará revocación cuando corresponda y publicará una versión corregida.

Los requisitos de preparación y controles pendientes están documentados en
[`packaging/windows/SIGNPATH_REQUIREMENTS.md`](packaging/windows/SIGNPATH_REQUIREMENTS.md).
