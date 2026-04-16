@echo off
setlocal enabledelayedexpansion

cd /d "C:\Users\jihen\Downloads\portfolio"

REM Compile CreateDTOs.java
echo Compiling CreateDTOs.java...
javac CreateDTOs.java

if %ERRORLEVEL% neq 0 (
    echo Compilation failed!
    exit /b 1
)

REM Run CreateDTOs
echo Running CreateDTOs...
java CreateDTOs

if %ERRORLEVEL% neq 0 (
    echo Execution failed!
    exit /b 1
)

echo Done!
pause
