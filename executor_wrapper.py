#!/usr/bin/env python3
import sys
import os

# Change to portfolio directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')
sys.path.insert(0, r'C:\Users\jihen\Downloads\portfolio')

# Import and run the master setup
try:
    import master_dto_setup
    sys.exit(0)
except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
