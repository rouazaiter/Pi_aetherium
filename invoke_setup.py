#!/usr/bin/env python3
import subprocess
import sys

result = subprocess.run(
    [sys.executable, r"C:\Users\jihen\Downloads\portfolio\final_dto_setup_executor.py"],
    capture_output=False,
    text=True
)

sys.exit(result.returncode)
