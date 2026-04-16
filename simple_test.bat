@echo off
REM Simple test of Python execution
cd /d C:\Users\jihen\Downloads\portfolio

REM Write test output
echo Test: Running create_dto_final.py > test_output.txt
echo. >> test_output.txt

REM Run Python script and append output
python create_dto_final.py >> test_output.txt 2>&1

REM Display result
type test_output.txt

echo.
echo Checking if dto directory was created...
if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo SUCCESS: Files were created!
    dir "src\main\java\jihen\portfolio\dto\"
) else (
    echo Files not found in dto directory
)
