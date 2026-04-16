#!/usr/bin/env python3
import subprocess
import sys
import os

os.chdir(r'C:\Users\jihen\Downloads\portfolio')

print("=" * 70)
print("COMPILING CreateDtoFiles.java")
print("=" * 70)

# Compile
compile_result = subprocess.run(['javac', 'CreateDtoFiles.java'], 
                               capture_output=True, text=True)
if compile_result.returncode != 0:
    print("ERROR: Compilation failed!")
    print(compile_result.stderr)
    sys.exit(1)

print("✓ Compilation successful")

print("\n" + "=" * 70)
print("RUNNING CreateDtoFiles")
print("=" * 70)

# Run
run_result = subprocess.run(['java', 'CreateDtoFiles'], 
                           capture_output=True, text=True)
print(run_result.stdout)

if run_result.stderr:
    print("STDERR:", run_result.stderr)

sys.exit(run_result.returncode)
