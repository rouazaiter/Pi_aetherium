@echo off
echo ============================================
echo   Whisper Transcription Service Launcher
echo ============================================
echo.

cd /d "%~dp0"

REM Check if venv exists
if not exist "venv" (
    echo [1/3] Creating virtual environment...
    python -m venv venv
)

echo [2/3] Installing dependencies...
call venv\Scripts\activate
pip install -r requirements.txt

echo.
echo [3/3] Starting Whisper service on http://localhost:8000 ...
echo.
python main.py

pause