@echo off
setlocal enabledelayedexpansion
cd /d "C:\Users\jihen\Downloads\portfolio"

echo.
echo ============================================================
echo STEP 1: COMPILING SetupDTOFiles.java
echo ============================================================
javac SetupDTOFiles.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!

echo.
echo ============================================================
echo STEP 2: RUNNING SetupDTOFiles
echo ============================================================
java SetupDTOFiles

echo.
echo ============================================================
echo STEP 3: VERIFYING CREATED FILES
echo ============================================================
set "DTO_DIR=C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
if exist "!DTO_DIR!" (
    echo Directory exists: !DTO_DIR!
    echo.
    echo Contents of directory:
    dir "!DTO_DIR!"
    
    echo.
    echo ============================================================
    echo SkillNodeDto.java
    echo ============================================================
    type "!DTO_DIR!\SkillNodeDto.java"
    
    echo.
    echo ============================================================
    echo PortfolioDNADto.java
    echo ============================================================
    type "!DTO_DIR!\PortfolioDNADto.java"
) else (
    echo ERROR: Directory not found: !DTO_DIR!
)

echo.
echo ============================================================
echo VERIFICATION COMPLETE
echo ============================================================
pause
