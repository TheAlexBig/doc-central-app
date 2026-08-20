# Licencias por instalación

Central Docs requiere una licencia permanente firmada y vinculada al código de
una computadora. La verificación funciona sin conexión.

## Claves

- La clave pública está incluida en la aplicación y únicamente verifica firmas.
- La clave privada se encuentra fuera del repositorio en
  `~/.central-docs-licensing/license-private-key.pem`.
- Nunca se debe subir, enviar ni copiar la clave privada al proyecto o a un
  cliente. Mantenga una copia de seguridad cifrada; si se pierde, no podrán
  emitirse nuevas licencias compatibles.

## Emitir una licencia

Solicite al cliente el código mostrado en la pantalla de activación. Desde
`doc-central-app`, ejecute:

```bash
./mvnw -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=com.big.dreamer.doccentral.license.tool.LicenseGenerator \
  -Dexec.args='--customer "Nombre del cliente" --machine "CD-CODIGO" --output "/ruta/cliente.license"'
```

En Windows utilice `mvnw.cmd`. Entregue únicamente el archivo `.license`.

## Activación

1. El cliente instala y abre Central Docs.
2. Le envía el código del equipo mostrado.
3. Usted genera y entrega su archivo `.license`.
4. El cliente elige **Importar archivo de licencia**.

La licencia operativa se guarda en `%LOCALAPPDATA%\Central Docs\license.json` y
se crea un comprobante en `%USERPROFILE%\Documents\Central Docs\license.json`.
Las actualizaciones conservan ambos archivos.
