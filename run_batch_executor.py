import subprocess
import sys
import os

# Change to the portfolio directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Execute the batch file
result = subprocess.run(
    [r"C:\Users\jihen\Downloads\portfolio\execute_dto_files.bat"],
    capture_output=True,
    text=True
)

# Print the output
print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)

# Exit with the same code as the batch file
sys.exit(result.returncode)
