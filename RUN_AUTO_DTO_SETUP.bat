@echo off
setlocal enabledelayedexpansion

echo.
echo ╔════════════════════════════════════════════════════════════════════════════════╗
echo ║                                                                                ║
echo ║                   DTO SETUP - AUTOMATIC EXECUTION                             ║
echo ║                                                                                ║
echo ╚════════════════════════════════════════════════════════════════════════════════╝
echo.

cd /d "C:\Users\jihen\Downloads\portfolio"

REM Run the Python script
python AUTO_RUN_DTO_SETUP.py

pause
