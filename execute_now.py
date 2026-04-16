#!/usr/bin/env python3
"""
Execute the DTO creation script with full output
"""
import subprocess
import sys

try:
    result = subprocess.run(
        [sys.executable, r"C:\Users\jihen\Downloads\portfolio\run_dto_creation.py"],
        cwd=r"C:\Users\jihen\Downloads\portfolio",
        capture_output=False,
        text=True
    )
    sys.exit(result.returncode)
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
