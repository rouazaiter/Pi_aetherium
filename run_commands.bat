@echo off
cd /d C:\Users\jihen\Downloads\portfolio
echo Compiling SetupDTOFiles.java...
javac SetupDTOFiles.java
if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    exit /b 1
)
echo.
echo Running SetupDTOFiles...
java SetupDTOFiles
if %ERRORLEVEL% NEQ 0 (
    echo Execution failed!
    exit /b 1
)
echo.
echo Listing dto directory...
dir "src\main\java\jihen\portfolio\dto"
echo.
echo Displaying SkillNodeDto.java...
type "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"
echo.
echo Displaying PortfolioDNADto.java...
type "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"
