import subprocess
import os
import pathlib

# Change to the portfolio directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Step 1: Compile
print("=" * 70)
print("STEP 1: COMPILING SetupDTOFiles.java")
print("=" * 70)
result = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True)
if result.returncode == 0:
    print("✓ Compilation successful!")
else:
    print(f"✗ Compilation failed!")
    print(result.stderr)
    exit(1)

# Step 2: Run
print("\n" + "=" * 70)
print("STEP 2: RUNNING SetupDTOFiles")
print("=" * 70)
result = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True)
print(result.stdout)
if result.returncode != 0:
    print(result.stderr)
    exit(1)

# Step 3: Verify directory contents
print("\n" + "=" * 70)
print("STEP 3: VERIFYING CREATED FILES")
print("=" * 70)
dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
print(f"\nDirectory: {dto_dir}")
print("Files:")
if os.path.exists(dto_dir):
    for f in os.listdir(dto_dir):
        print(f"  - {f}")
else:
    print("  ERROR: Directory does not exist!")

# Step 4: Show file contents
print("\n" + "=" * 70)
print("STEP 4: DISPLAYING FILE CONTENTS")
print("=" * 70)

# SkillNodeDto.java
skill_file = os.path.join(dto_dir, 'SkillNodeDto.java')
if os.path.exists(skill_file):
    print(f"\n--- SkillNodeDto.java ---")
    with open(skill_file, 'r') as f:
        print(f.read())

# PortfolioDNADto.java
dna_file = os.path.join(dto_dir, 'PortfolioDNADto.java')
if os.path.exists(dna_file):
    print(f"\n--- PortfolioDNADto.java ---")
    with open(dna_file, 'r') as f:
        print(f.read())

print("\n" + "=" * 70)
print("✓ ALL STEPS COMPLETED SUCCESSFULLY")
print("=" * 70)
