@echo off
REM Compile and execute SetupDTOFiles.java to create the DTO directory and files

setlocal enabledelayedexpansion

cd /d C:\Users\jihen\Downloads\portfolio

echo ========================================================
echo DTO SETUP EXECUTION SCRIPT
echo ========================================================
echo.
echo Compiling SetupDTOFiles.java...

REM Compile the Java class
javac SetupDTOFiles.java

if %ERRORLEVEL% EQU 0 (
    echo ✓ Compilation successful
    echo.
    echo Executing SetupDTOFiles...
    java SetupDTOFiles
) else (
    echo ✗ Compilation failed
    exit /b 1
)

echo.
echo ========================================================
echo END OF EXECUTION
echo ========================================================
