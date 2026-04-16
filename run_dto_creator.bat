@echo off
cd /d C:\Users\jihen\Downloads\portfolio
echo Compiling DTOCreator.java...
javac DTOCreator.java
if errorlevel 1 (
    echo Compilation failed
    exit /b 1
)
echo Running DTOCreator...
java DTOCreator
if errorlevel 1 (
    echo Execution failed
    exit /b 1
)
echo.
echo Done!
