#!/usr/bin/env python3
import os
import sys
import subprocess

# Change to project directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Compile SetupDTO.java
print("Compiling SetupDTO.java...")
result = subprocess.run(['javac', 'SetupDTO.java'], capture_output=True, text=True)
if result.returncode != 0:
    print("Compilation failed:")
    print(result.stderr)
    sys.exit(1)

print("Compilation successful!")

# Run SetupDTO
print("\nRunning SetupDTO...")
result = subprocess.run(['java', 'SetupDTO'], capture_output=True, text=True)
print(result.stdout)
if result.stderr:
    print("Errors:")
    print(result.stderr)

print("\nVerifying files...")
dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
if os.path.exists(dto_path):
    print(f"✓ Directory exists: {dto_path}")
    files = os.listdir(dto_path)
    print(f"✓ Files in directory: {files}")
else:
    print(f"✗ Directory does not exist: {dto_path}")
