import os
import subprocess
import sys

# Change to the portfolio directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Run the execute_final_dto_now.py script
result = subprocess.run([sys.executable, "execute_final_dto_now.py"], 
                       capture_output=True, 
                       text=True)

# Print all output
print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)

print(f"\nReturn code: {result.returncode}")
