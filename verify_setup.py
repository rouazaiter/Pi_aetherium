import subprocess
import os

os.chdir(r'C:\Users\jihen\Downloads\portfolio')

# Compile
print("=== Compiling SetupDTOFiles.java ===")
result = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True)
print(result.stdout)
if result.stderr:
    print("Error:", result.stderr)
if result.returncode == 0:
    print("✓ Compilation successful\n")
    
    # Run
    print("=== Running SetupDTOFiles ===")
    result = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True)
    print(result.stdout)
    if result.stderr:
        print("Error:", result.stderr)
    
    # Verify - List directory
    dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
    if os.path.exists(dto_path):
        print("\n=== Listing DTO directory contents ===")
        files = os.listdir(dto_path)
        for f in files:
            print(f"  - {f}")
        
        # Show SkillNodeDto content
        skill_file = os.path.join(dto_path, 'SkillNodeDto.java')
        if os.path.exists(skill_file):
            print("\n=== SkillNodeDto.java content ===")
            with open(skill_file, 'r') as f:
                print(f.read())
        
        # Show PortfolioDNADto content
        dna_file = os.path.join(dto_path, 'PortfolioDNADto.java')
        if os.path.exists(dna_file):
            print("\n=== PortfolioDNADto.java content ===")
            with open(dna_file, 'r') as f:
                print(f.read())
    else:
        print(f"Directory not found: {dto_path}")
else:
    print("✗ Compilation failed")
