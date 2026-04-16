@echo off
REM Direct execution of Python DTO script
REM This batch file executes the direct_dto_creation.py script
REM and captures the output

cd /d C:\Users\jihen\Downloads\portfolio

echo ===================================================================== 
echo DTO CREATION SCRIPT EXECUTION
echo =====================================================================
echo.
echo Starting Python script: direct_dto_creation.py
echo Working Directory: %CD%
echo.

REM Execute the Python script
python direct_dto_creation.py

echo.
echo ===================================================================== 
echo EXECUTION COMPLETE
echo =====================================================================
echo.
echo Verifying DTO directory and files...
echo.

REM Check if directory exists
if exist "src\main\java\jihen\portfolio\dto" (
    echo ✓ DTO directory exists: src\main\java\jihen\portfolio\dto
    echo.
    echo Directory contents:
    dir /B "src\main\java\jihen\portfolio\dto"
    echo.
) else (
    echo ERROR: DTO directory was not created
)

REM Check if files exist
if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo ✓ SkillNodeDto.java exists
) else (
    echo ERROR: SkillNodeDto.java not found
)

if exist "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java" (
    echo ✓ PortfolioDNADto.java exists
) else (
    echo ERROR: PortfolioDNADto.java not found
)

echo.
echo ===================================================================== 
echo END OF BATCH EXECUTION
echo =====================================================================
pause
