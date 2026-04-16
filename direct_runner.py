#!/usr/bin/env python3
import subprocess
import sys
import os

os.chdir(r"C:\Users\jihen\Downloads\portfolio")
result = subprocess.run([sys.executable, "EXECUTE_DTO_CREATION.py"])
sys.exit(result.returncode)
