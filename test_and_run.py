#!/usr/bin/env python3
"""
Test and execute the DTO creation script
Captures output to a file for display
"""
import subprocess
import sys
import os

os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Method 1: Try running the script directly
print("Attempting to execute create_dto_final.py...")
print("=" * 70)

try:
    # Use Popen with explicit parameters
    proc = subprocess.Popen(
        [sys.executable, "create_dto_final.py"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        universal_newlines=True,
        cwd=r"C:\Users\jihen\Downloads\portfolio"
    )
    
    # Read output line by line
    all_output = []
    for line in proc.stdout:
        print(line, end='', flush=True)
        all_output.append(line)
    
    # Wait for completion
    return_code = proc.wait()
    
    # Save output to file for reference
    with open("script_output.log", "w") as f:
        f.writelines(all_output)
    
    print("\n" + "=" * 70)
    print(f"Script completed with return code: {return_code}")
    print("Output saved to: script_output.log")
    
    sys.exit(return_code)
    
except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
