@echo off
REM Run the Python script and capture output to a file
cd /d C:\Users\jihen\Downloads\portfolio
python create_dto_final.py > output.txt 2>&1
REM Display the output
type output.txt
