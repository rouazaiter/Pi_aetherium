@echo off
cd /d "C:\Users\jihen\Downloads\portfolio"

echo.
echo ============================================================
echo COMPILING CreateDtoFiles.java
echo ============================================================
javac CreateDtoFiles.java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Compilation failed!
    exit /b 1
)
echo Compilation successful!

echo.
echo ============================================================
echo RUNNING CreateDtoFiles
echo ============================================================
java CreateDtoFiles

echo.
echo.
echo ============================================================
echo Program execution complete!
echo ============================================================
pause
