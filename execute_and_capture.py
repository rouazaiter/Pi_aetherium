#!/usr/bin/env python3
"""Execute the create_dto_final.py script with full output capture"""
import os
import sys
import subprocess

os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Run the script
result = subprocess.Popen(
    [sys.executable, "create_dto_final.py"],
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True
)

# Read and print output
for line in result.stdout:
    print(line, end='')

result.wait()
sys.exit(result.returncode)
