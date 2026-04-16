#!/usr/bin/env python3
import os
import subprocess
import sys

# Change to the project directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Execute the batch file
result = subprocess.run(['cmd.exe', '/c', 'EXECUTE_FINAL_DTO_SETUP.bat'], 
                       capture_output=True, text=True)

print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)
    
sys.exit(result.returncode)
