import subprocess
import sys
import os

os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Compile SetupDTO.java
print("Compiling SetupDTO.java...")
result = subprocess.run(['javac', 'SetupDTO.java'], capture_output=True, text=True)
if result.returncode != 0:
    print("Compilation error:")
    print(result.stderr)
    sys.exit(1)

print("✓ Compilation successful")

# Run SetupDTO
print("\nRunning SetupDTO...")
result = subprocess.run(['java', 'SetupDTO'], capture_output=True, text=True)
print(result.stdout)
if result.stderr:
    print("Errors:", result.stderr)

# Verify files were created
dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
print(f"\n✓ Verifying files in {dto_path}")
if os.path.exists(dto_path):
    files = os.listdir(dto_path)
    for f in files:
        print(f"  - {f}")
else:
    print("ERROR: DTO directory not found!")
