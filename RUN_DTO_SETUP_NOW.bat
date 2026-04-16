@echo off
REM ================================================================
REM DTO SETUP EXECUTION BATCH FILE
REM ================================================================
REM This batch file will execute the Python script to create DTO files
REM ================================================================

echo.
echo ================================================================
echo DTO File Setup - Starting Execution
echo ================================================================
echo.

REM Change to the portfolio directory
cd /d "C:\Users\jihen\Downloads\portfolio"

echo Current directory: %CD%
echo.

REM Try to run with python3 first, then python
echo Attempting to execute final_dto_setup.py...
echo.

python final_dto_setup.py

if errorlevel 1 (
    echo.
    echo ERROR: Python execution failed. 
    echo Please ensure Python 3 is installed and in your PATH.
    echo.
    echo Alternative: You can manually run:
    echo   cd C:\Users\jihen\Downloads\portfolio
    echo   python final_dto_setup.py
    echo.
    pause
    exit /b 1
)

echo.
echo ================================================================
echo DTO Setup Completed Successfully!
echo ================================================================
echo.
pause
