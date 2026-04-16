@echo off
cd /d "C:\Users\jihen\Downloads\portfolio"

REM Create directories
for /F "delims=" %%A in ('echo prompt $H ^| cmd') do set "BS=%%A"
cls

echo Creating directory structure...
mkdir "src\main\java\jihen\portfolio\dto" 2>nul

echo Compiling and running SetupDTOFiles.java...
javac SetupDTOFiles.java
java SetupDTOFiles

echo.
echo Verification - Directory contents:
dir "src\main\java\jihen\portfolio\dto\"

echo.
echo.
echo Files created successfully!
