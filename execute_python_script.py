#!/usr/bin/env python3
import os
import sys

os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Execute create_dto_final.py and redirect output to a log file
import subprocess
result = subprocess.run([sys.executable, "create_dto_final.py"], 
                       capture_output=False, 
                       text=True,
                       cwd=r"C:\Users\jihen\Downloads\portfolio")

sys.exit(result.returncode)
