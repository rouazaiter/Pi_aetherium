import os
import sys

path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
try:
    os.makedirs(path, exist_ok=True)
    if os.path.isdir(path):
        print(f"SUCCESS: Directory created at {path}")
        sys.exit(0)
    else:
        print(f"ERROR: Directory does not exist after creation attempt")
        sys.exit(1)
except Exception as e:
    print(f"ERROR: {e}")
    sys.exit(1)
