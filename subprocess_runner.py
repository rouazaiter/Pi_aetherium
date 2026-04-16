#!/usr/bin/env python3
import os
import subprocess

# Navigate to portfolio directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Run the script using subprocess which should work without PowerShell
print("Executing: python create_dto_final.py")
print("=" * 70)

result = subprocess.run(
    ["python", "create_dto_final.py"],
    shell=False,
    text=True
)

sys.exit(result.returncode)
