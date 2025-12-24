@echo off
setlocal
cd /d "%~dp0"

echo Building JAR (PacMan.jar)...
call build.bat
if errorlevel 1 (
    echo Build failed. Exiting.
    exit /b 1
)

echo Preparing jpackage options (app-image, no WiX required)...
set "ICON_OPT="
if exist pacman.ico set "ICON_OPT=--icon pacman.ico"

if not exist dist mkdir dist

rem Create self-contained app-image with bundled runtime (no installer)
jpackage ^
  --type app-image ^
  --input . ^
  --dest dist ^
  --name PacMan ^
  --app-version 1.0.0 ^
  --main-jar PacMan.jar ^
  --main-class App ^
  --vendor "PacMan" ^
  --description "PacMan desktop game" ^
  %ICON_OPT% ^
  --verbose

if errorlevel 1 (
    echo jpackage failed. Exiting.
    exit /b 1
)

echo.
echo ==================================================
echo App image created at: dist\PacMan
if exist dist\PacMan\PacMan.exe (
    echo Double-click dist\PacMan\PacMan.exe to run (no Java needed).
)
echo To zip it: powershell -NoLogo -Command "Compress-Archive dist\PacMan dist\PacMan-portable.zip -Force"
echo ==================================================

echo Done.
endlocal
