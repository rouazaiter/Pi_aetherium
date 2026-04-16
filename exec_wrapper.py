#!/usr/bin/env python3
"""
DTO Execution Wrapper - Executes direct_dto_creation.py and captures output
"""
import subprocess
import sys
import os

os.chdir(r"C:\Users\jihen\Downloads\portfolio")

try:
    result = subprocess.run([sys.executable, "direct_dto_creation.py"], 
                          capture_output=True, 
                          text=True, 
                          timeout=30)
    
    print(result.stdout)
    if result.stderr:
        print("STDERR:", result.stderr)
    
    sys.exit(result.returncode)
except Exception as e:
    print(f"Error: {e}")
    sys.exit(1)
