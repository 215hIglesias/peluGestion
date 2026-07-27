#!/usr/bin/env bash
# ============================================================
#  Genera el paquete de Windows (32 y 64 bits) desde el Mac.
#  Resultado: dist/PeluGestion-Windows.zip  (listo para copiar
#  a cualquier PC con Windows y abrir con doble clic).
#
#  Uso:   bash packaging/build-windows-package.sh
# ============================================================
set -euo pipefail

# --- Java de 32 bits (BellSoft Liberica). Corre en Win 32 y 64 bits ---
JRE_VERSION="17.0.20+10"
JRE_FILE="bellsoft-jre${JRE_VERSION}-windows-i586-full.zip"
JRE_URL="https://github.com/bell-sw/Liberica/releases/download/${JRE_VERSION}/${JRE_FILE}"

# --- Rutas ---
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
BUILD="$ROOT/build/win-pkg"
STAGE="$BUILD/PeluGestion"
CACHE="$ROOT/build/cache"
JAR="PeluGestion-1.8-all.jar"

echo "==> 1/5  Compilando el JAR..."
( cd "$ROOT" && ./gradlew fatJar --console=plain )

echo "==> 2/5  Descargando Java 32-bit (solo la primera vez)..."
mkdir -p "$CACHE"
if [ ! -f "$CACHE/$JRE_FILE" ]; then
    curl -L --fail -o "$CACHE/$JRE_FILE" "$JRE_URL"
else
    echo "    (ya estaba descargado en cache)"
fi

echo "==> 3/5  Preparando la carpeta del paquete..."
rm -rf "$BUILD"
mkdir -p "$STAGE"
unzip -q "$CACHE/$JRE_FILE" -d "$STAGE/_jre_tmp"
# La zip trae una carpeta tipo "jdk-17..." o "jre-17..."; la renombramos a "jre"
JRE_INNER="$(find "$STAGE/_jre_tmp" -maxdepth 1 -mindepth 1 -type d | head -1)"
mv "$JRE_INNER" "$STAGE/jre"
rm -rf "$STAGE/_jre_tmp"

echo "==> 4/5  Copiando app y lanzadores..."
cp "$ROOT/build/libs/$JAR" "$STAGE/"
cp "$ROOT/packaging/windows/PeluGestion.bat" "$STAGE/"
cp "$ROOT/packaging/windows/PeluGestion-diagnostico.bat" "$STAGE/"
cp "$ROOT/packaging/windows/LEEME.txt" "$STAGE/"

echo "==> 5/5  Comprimiendo..."
mkdir -p "$ROOT/dist"
rm -f "$ROOT/dist/PeluGestion-Windows.zip"
( cd "$BUILD" && zip -qr "$ROOT/dist/PeluGestion-Windows.zip" "PeluGestion" )

echo ""
echo "LISTO  ->  dist/PeluGestion-Windows.zip"
du -h "$ROOT/dist/PeluGestion-Windows.zip" | cut -f1 | xargs echo "Tamano:"
echo "Copialo a un Windows, descomprime y doble clic en PeluGestion.bat"
