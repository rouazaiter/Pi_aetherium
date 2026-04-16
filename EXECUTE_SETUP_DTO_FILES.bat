@echo off
setlocal enabledelayedexpansion
cd /d "C:\Users\jihen\Downloads\portfolio"

REM ========================================================================
REM STEP 1: COMPILE SetupDTOFiles.java
REM ========================================================================
echo.
echo ========================================================================
echo STEP 1: COMPILING SetupDTOFiles.java
echo ========================================================================
echo.

javac SetupDTOFiles.java
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilation failed!
    pause
    exit /b 1
)
echo Compilation successful!

REM ========================================================================
REM STEP 2: RUN SetupDTOFiles
REM ========================================================================
echo.
echo ========================================================================
echo STEP 2: RUNNING java SetupDTOFiles
echo ========================================================================
echo.

java SetupDTOFiles

REM ========================================================================
REM STEP 3: LIST DIRECTORY CONTENTS
REM ========================================================================
echo.
echo ========================================================================
echo STEP 3: LISTING CONTENTS OF DTO DIRECTORY
echo ========================================================================
echo.
echo Directory: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
echo.
echo Files:
dir "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" /b

REM ========================================================================
REM STEP 4: DISPLAY FILE CONTENTS
REM ========================================================================
echo.
echo ========================================================================
echo STEP 4: FILE CONTENTS - SkillNodeDto.java
echo ========================================================================
echo.
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo.
echo.
echo ========================================================================
echo STEP 4 (continued): FILE CONTENTS - PortfolioDNADto.java
echo ========================================================================
echo.
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo ========================================================================
echo ALL STEPS COMPLETED SUCCESSFULLY!
echo ========================================================================
echo.
pause
