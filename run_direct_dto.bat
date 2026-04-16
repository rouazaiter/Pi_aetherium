@echo off
setlocal enabledelayedexpansion
cd /d C:\Users\jihen\Downloads\portfolio

echo Running Python script...
python direct_run_dto.py

if errorlevel 1 (
    echo Script failed with error code !errorlevel!
    exit /b 1
)

echo.
echo ============================================
echo Script execution completed successfully
echo ============================================

endlocal
