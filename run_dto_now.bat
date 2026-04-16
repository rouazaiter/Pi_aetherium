@echo off
cd /d "C:\Users\jihen\Downloads\portfolio"
echo Executing DTO creation script...
python dto_creation_complete.py
echo.
echo Checking log file...
if exist dto_creation_log.txt (
    echo.
    echo ===== DTO CREATION LOG =====
    type dto_creation_log.txt
) else (
    echo ERROR: Log file not created
)
