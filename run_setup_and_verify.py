import os
import subprocess

# Change to the correct directory
os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Step 1: Compile SetupDTOFiles.java
print("=" * 60)
print("STEP 1: Compiling SetupDTOFiles.java")
print("=" * 60)
result = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True)
print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)
if result.returncode != 0:
    print("✗ Compilation failed with return code", result.returncode)
    exit(1)
print("✓ Compilation successful\n")

# Step 2: Run SetupDTOFiles
print("=" * 60)
print("STEP 2: Running SetupDTOFiles")
print("=" * 60)
result = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True)
print(result.stdout)
if result.stderr:
    print("STDERR:", result.stderr)
print()

# Step 3: Verify the output
print("=" * 60)
print("STEP 3: Verifying created files")
print("=" * 60)

dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
print(f"\nChecking directory: {dto_path}")

if os.path.exists(dto_path):
    print("✓ Directory exists!\n")
    
    # List files
    print("Directory contents:")
    files = os.listdir(dto_path)
    for f in sorted(files):
        file_path = os.path.join(dto_path, f)
        if os.path.isfile(file_path):
            size = os.path.getsize(file_path)
            print(f"  - {f} ({size} bytes)")
    
    # Show SkillNodeDto.java
    skill_file = os.path.join(dto_path, 'SkillNodeDto.java')
    if os.path.exists(skill_file):
        print(f"\n{'-'*60}")
        print("SkillNodeDto.java content:")
        print(f"{'-'*60}")
        with open(skill_file, 'r', encoding='utf-8') as f:
            print(f.read())
    else:
        print("✗ SkillNodeDto.java not found")
    
    # Show PortfolioDNADto.java
    dna_file = os.path.join(dto_path, 'PortfolioDNADto.java')
    if os.path.exists(dna_file):
        print(f"\n{'-'*60}")
        print("PortfolioDNADto.java content:")
        print(f"{'-'*60}")
        with open(dna_file, 'r', encoding='utf-8') as f:
            print(f.read())
    else:
        print("✗ PortfolioDNADto.java not found")
else:
    print(f"✗ Directory does not exist: {dto_path}")
