@echo off
rem ============================================================
rem  PeluGestion - Lanzador para Windows (32 y 64 bits)
rem  Usa el Java incluido en la carpeta "jre".
rem  javaw.exe = sin ventana negra de consola.
rem ============================================================
cd /d "%~dp0"
for %%f in (PeluGestion-*-all.jar) do set JAR=%%f
start "" "jre\bin\javaw.exe" -jar "%JAR%"
