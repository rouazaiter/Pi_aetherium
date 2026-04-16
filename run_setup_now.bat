@echo off
setlocal enabledelayedexpansion

cd /d C:\Users\jihen\Downloads\portfolio

REM Compile and run SetupDTOFiles
echo Compiling SetupDTOFiles.java...
javac SetupDTOFiles.java

echo.
echo Running SetupDTOFiles...
java SetupDTOFiles

echo.
echo Done!
