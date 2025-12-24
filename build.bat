@echo off
echo Building PacMan standalone application...

REM Compile the Java files
cd /d "%~dp0"
javac -cp src -d bin src\*.java

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Copy resources to bin directory
xcopy /Y /Q src\*.png bin\ 2>nul
xcopy /Y /Q src\*.wav bin\ 2>nul

REM Create the JAR file
jar cfm PacMan.jar MANIFEST.MF -C bin .

if errorlevel 1 (
    echo JAR creation failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful!
echo ========================================
echo.
echo Executable JAR created: PacMan.jar
echo.
echo To run the application:
echo   - Double-click PacMan.jar
echo   - Or run: java -jar PacMan.jar
echo.
pause
