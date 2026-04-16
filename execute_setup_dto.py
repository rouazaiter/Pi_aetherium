#!/usr/bin/env python3
import subprocess
import os
import sys

os.chdir(r'C:\Users\jihen\Downloads\portfolio')

print("=" * 60)
print("COMPILING SetupDTOFiles.java")
print("=" * 60)

# Compile
compile_result = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True)
if compile_result.returncode != 0:
    print(f"ERROR: Compilation failed!")
    print(compile_result.stderr)
    sys.exit(1)
print("✓ Compilation successful!")

print("\n" + "=" * 60)
print("RUNNING SetupDTOFiles")
print("=" * 60 + "\n")

# Run
run_result = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True)
print(run_result.stdout)
if run_result.returncode != 0:
    print(f"ERROR: Execution failed!")
    print(run_result.stderr)
    sys.exit(1)

print("\n" + "=" * 60)
print("VERIFYING OUTPUT")
print("=" * 60)

dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
if os.path.exists(dto_dir):
    print(f"\n✓ Directory exists: {dto_dir}")
    files = os.listdir(dto_dir)
    print(f"\nFiles in directory:")
    for f in files:
        print(f"  - {f}")
    
    # Show file contents
    print("\n" + "=" * 60)
    print("FILE CONTENTS")
    print("=" * 60)
    
    for filename in ['SkillNodeDto.java', 'PortfolioDNADto.java']:
        filepath = os.path.join(dto_dir, filename)
        if os.path.exists(filepath):
            print(f"\n--- {filename} ---")
            with open(filepath, 'r') as f:
                print(f.read())
else:
    print(f"ERROR: Directory not created: {dto_dir}")
    sys.exit(1)

print("\n" + "=" * 60)
print("✓ ALL STEPS COMPLETED SUCCESSFULLY")
print("=" * 60)
