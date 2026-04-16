#!/usr/bin/env python3
import subprocess
import sys
import os

os.chdir(r"C:\Users\jihen\Downloads\portfolio")
result = subprocess.run([sys.executable, "create_dto_final.py"], capture_output=False, text=True)
sys.exit(result.returncode)
