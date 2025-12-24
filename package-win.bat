@echo off
setlocal
cd /d "%~dp0"

echo Building JAR (PacMan.jar)...
call build.bat
if errorlevel 1 (
    echo Build failed. Exiting.
    exit /b 1
)

echo Preparing jpackage options...
set "ICON_OPT="
if exist pacman.ico set "ICON_OPT=--icon pacman.ico"

REM Create dist folder for output
if not exist dist mkdir dist

echo Running jpackage to create Windows EXE with bundled runtime...
jpackage ^
  --type exe ^
  --input . ^
  --dest dist ^
  --name PacMan ^
  --app-version 1.0.0 ^
  --main-jar PacMan.jar ^
  --main-class App ^
  --vendor "PacMan" ^
  --description "PacMan desktop game" ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser ^
  --win-per-user-install ^
  %ICON_OPT% ^
  --verbose

if errorlevel 1 (
    echo jpackage failed. Exiting.
    exit /b 1
)

echo.
echo ==================================================
echo Windows installer created under: dist\PacMan-1.0.0.exe
if exist dist\PacMan-1.0.0.exe (
    echo Double-click dist\PacMan-1.0.0.exe to install.
) else (
    echo Check dist folder for the generated installer.
)
echo ==================================================

echo Done.
endlocal
