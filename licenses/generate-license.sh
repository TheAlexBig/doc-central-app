#!/usr/bin/env bash

set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
APP_DIR="$(cd -- "$SCRIPT_DIR/.." && pwd)"
PRIVATE_KEY="$HOME/.central-docs-licensing/license-private-key.pem"

customer="${1:-}"
machine="${2:-}"

if [[ -z "$customer" ]]; then
  read -r -p "Nombre del cliente: " customer
fi

if [[ -z "$machine" ]]; then
  read -r -p "Codigo del equipo (CD-...): " machine
fi

customer="${customer#"${customer%%[![:space:]]*}"}"
customer="${customer%"${customer##*[![:space:]]}"}"
machine="${machine^^}"
machine="${machine//[[:space:]]/}"

if [[ -z "$customer" ]]; then
  echo "Error: el nombre del cliente no puede estar vacio." >&2
  exit 1
fi

if [[ "$customer" == *'"'* ]]; then
  echo 'Error: el nombre del cliente no puede contener comillas dobles (").' >&2
  exit 1
fi

if [[ ! "$machine" =~ ^CD-[A-F0-9]{24}$ ]]; then
  echo "Error: el codigo debe tener el formato CD- seguido de 24 caracteres hexadecimales." >&2
  exit 1
fi

if [[ ! -f "$PRIVATE_KEY" ]]; then
  echo "Error: no se encontro la clave privada en $PRIVATE_KEY" >&2
  exit 1
fi

file_name="$(printf '%s' "$customer" \
  | iconv -f UTF-8 -t ASCII//TRANSLIT 2>/dev/null \
  | tr '[:upper:]' '[:lower:]' \
  | sed -E 's/[^a-z0-9]+/-/g; s/^-+|-+$//g')"

if [[ -z "$file_name" ]]; then
  file_name="cliente"
fi

output="$SCRIPT_DIR/$file_name.license"

if [[ -e "$output" ]]; then
  read -r -p "Ya existe $output. Deseas reemplazarlo? [s/N]: " answer
  if [[ ! "$answer" =~ ^[sS]$ ]]; then
    echo "Operacion cancelada."
    exit 0
  fi
fi

cd "$APP_DIR"
bash ./mvnw -q org.codehaus.mojo:exec-maven-plugin:3.5.0:java \
  -Dexec.mainClass=com.big.dreamer.doccentral.license.tool.LicenseGenerator \
  -Dexec.args="--customer \"$customer\" --machine \"$machine\" --output \"$output\""

echo
echo "Archivo listo para entregar: $output"
