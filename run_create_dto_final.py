#!/usr/bin/env python3
import os
import sys

os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Import and execute the create_dto_final module directly
with open("create_dto_final.py", "r") as f:
    code = f.read()

# Execute the code
exec(code)
