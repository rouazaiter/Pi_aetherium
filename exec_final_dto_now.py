#!/usr/bin/env python3
import subprocess
import sys
import os

# Change to portfolio directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Run final_dto_exec.py
result = subprocess.run([sys.executable, "final_dto_exec.py"], cwd=r"C:\Users\jihen\Downloads\portfolio")
sys.exit(result.returncode)
