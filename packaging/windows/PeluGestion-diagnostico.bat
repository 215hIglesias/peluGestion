@echo off
rem ============================================================
rem  Igual que PeluGestion.bat pero SI muestra la consola.
rem  Usalo si la app no arranca: aqui veras el error.
rem  (java.exe en vez de javaw.exe = deja la ventana abierta)
rem ============================================================
cd /d "%~dp0"
"jre\bin\java.exe" -jar "PeluGestion-1.11-all.jar"
echo.
echo ---- La aplicacion se ha cerrado. Pulsa una tecla. ----
pause >nul
