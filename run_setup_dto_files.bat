@echo off
cd /d "C:\Users\jihen\Downloads\portfolio"

echo.
echo ============================================================
echo COMPILING SetupDTOFiles.java
echo ============================================================
javac SetupDTOFiles.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    exit /b 1
)
echo Compilation successful!

echo.
echo ============================================================
echo RUNNING SetupDTOFiles
echo ============================================================
java SetupDTOFiles

echo.
echo ============================================================
echo EXECUTION COMPLETE
echo ============================================================

pause
