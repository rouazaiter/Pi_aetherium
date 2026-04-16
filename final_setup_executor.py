import os
import subprocess
import sys

# Navigate to working directory
portfolio_dir = r'C:\Users\jihen\Downloads\portfolio'
os.chdir(portfolio_dir)

# Ensure we're in the right place
print(f"Working directory: {os.getcwd()}")
print()

# Step 1: Compile SetupDTOFiles.java
print("="*70)
print("STEP 1: COMPILING SetupDTOFiles.java")
print("="*70)

try:
    compile_proc = subprocess.Popen(['javac', 'SetupDTOFiles.java'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    stdout, stderr = compile_proc.communicate()
    
    if compile_proc.returncode == 0:
        print("✓ Compilation successful!\n")
    else:
        print("✗ Compilation failed!")
        print("STDERR:", stderr)
        sys.exit(1)
except Exception as e:
    print(f"Error during compilation: {e}")
    sys.exit(1)

# Step 2: Run SetupDTOFiles
print("="*70)
print("STEP 2: RUNNING java SetupDTOFiles")
print("="*70 + "\n")

try:
    run_proc = subprocess.Popen(['java', 'SetupDTOFiles'], stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    stdout, stderr = run_proc.communicate()
    
    print(stdout)
    
    if run_proc.returncode != 0:
        print("STDERR:", stderr)
        sys.exit(1)
except Exception as e:
    print(f"Error during execution: {e}")
    sys.exit(1)

# Step 3: Verify the created directory
print("="*70)
print("STEP 3: LISTING CONTENTS OF DTO DIRECTORY")
print("="*70)

dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
print(f"\nDirectory path: {dto_path}\n")

if os.path.exists(dto_path):
    print("Files created:")
    files = os.listdir(dto_path)
    for filename in files:
        print(f"  ✓ {filename}")
    print()
else:
    print(f"✗ Directory does not exist: {dto_path}")
    sys.exit(1)

# Step 4: Display file contents
print("="*70)
print("STEP 4: FILE CONTENTS")
print("="*70)

# Read and display SkillNodeDto.java
skill_dto_path = os.path.join(dto_path, 'SkillNodeDto.java')
print(f"\n--- SkillNodeDto.java ---\n")
try:
    with open(skill_dto_path, 'r') as f:
        content = f.read()
        print(content)
except Exception as e:
    print(f"Error reading file: {e}")

# Read and display PortfolioDNADto.java
dna_dto_path = os.path.join(dto_path, 'PortfolioDNADto.java')
print(f"\n--- PortfolioDNADto.java ---\n")
try:
    with open(dna_dto_path, 'r') as f:
        content = f.read()
        print(content)
except Exception as e:
    print(f"Error reading file: {e}")

print("\n" + "="*70)
print("✓ ALL STEPS COMPLETED SUCCESSFULLY!")
print("="*70)
