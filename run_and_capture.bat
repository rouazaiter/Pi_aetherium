@echo off
REM Run the Python script to create DTO files
cd /d "C:\Users\jihen\Downloads\portfolio"
python direct_dto_creation.py > dto_output.txt 2>&1
type dto_output.txt
