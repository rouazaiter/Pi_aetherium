#!/usr/bin/env python3
import os
import sys

# Ensure the path can be created
path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(path, exist_ok=True)
print(f"Created directory: {path}")
print(f"Directory exists: {os.path.isdir(path)}")
