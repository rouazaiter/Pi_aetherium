@echo off
setlocal enabledelayedexpansion

cd /d "C:\Users\jihen\Downloads\portfolio"

echo.
echo ============================================================
echo Executing: create_dto_batch_now.bat
echo ============================================================
echo.

REM Run the batch file
call create_dto_batch_now.bat

REM Capture and show the files
if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo.
    echo ============================================================
    echo FILES CREATED SUCCESSFULLY
    echo ============================================================
    echo Showing directory contents:
    dir /B "src\main\java\jihen\portfolio\dto"
)

echo.
pause
