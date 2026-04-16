import os
import sys

dto_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_path, exist_ok=True)
print(f"Directory created: {dto_path}")
print(f"Directory exists: {os.path.exists(dto_path)}")

# List the directory
if os.path.exists(dto_path):
    print(f"Contents: {os.listdir(dto_path)}")
