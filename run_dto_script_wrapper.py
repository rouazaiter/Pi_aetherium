#!/usr/bin/env python3
import subprocess
import os

# Change to the portfolio directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Run the original create_dto_final.py script
result = subprocess.run([r"C:\Users\jihen\Downloads\portfolio\create_dto_final.py"], 
                       capture_output=True, 
                       text=True)

# Print all output
print(result.stdout)
if result.stderr:
    print("STDERR:")
    print(result.stderr)

print(f"\nReturn code: {result.returncode}")
