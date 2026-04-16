#!/usr/bin/env python3
import os
import sys

# Just test if we can run Python
print("Python is working!")
print(f"Python version: {sys.version}")
print(f"Current directory: {os.getcwd()}")

# Try to change to the target directory
target_dir = r"C:\Users\jihen\Downloads\portfolio"
if os.path.exists(target_dir):
    os.chdir(target_dir)
    print(f"Changed to: {os.getcwd()}")
    
    # Now run the actual script
    exec(open('final_dto_setup.py').read())
else:
    print(f"Directory not found: {target_dir}")
