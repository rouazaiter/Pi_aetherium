#!/usr/bin/env python3
import os
import sys

# Execute the direct_dto_creation script
sys.path.insert(0, r'C:\Users\jihen\Downloads\portfolio')

# Run the script
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Execute the original script
exec(open('direct_dto_creation.py').read())
