@echo off
REM Create the dto directory
mkdir "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" 2>nul
if exist "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" (
    echo SUCCESS: Directory created
) else (
    echo ERROR: Failed to create directory
)
