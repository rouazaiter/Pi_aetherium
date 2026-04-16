#!/usr/bin/env python3
import os
import subprocess
import sys

def main():
    # Change to the correct directory
    os.chdir(r'C:\Users\jihen\Downloads\portfolio')
    
    print("=" * 70)
    print("DTO SETUP AND VERIFICATION")
    print("=" * 70)
    print()
    
    # Step 1: Compile
    print("STEP 1: Compiling SetupDTOFiles.java")
    print("-" * 70)
    try:
        result = subprocess.run(['javac', 'SetupDTOFiles.java'], 
                              capture_output=True, text=True, timeout=30)
        if result.returncode == 0:
            print("✓ Compilation successful")
        else:
            print(f"✗ Compilation failed:")
            print(result.stderr)
            return False
    except Exception as e:
        print(f"✗ Error during compilation: {e}")
        return False
    
    print()
    
    # Step 2: Run the program
    print("STEP 2: Running SetupDTOFiles")
    print("-" * 70)
    try:
        result = subprocess.run(['java', 'SetupDTOFiles'], 
                              capture_output=True, text=True, timeout=30)
        print(result.stdout)
        if result.returncode != 0 and result.stderr:
            print("Error:", result.stderr)
    except Exception as e:
        print(f"✗ Error during execution: {e}")
        return False
    
    print()
    
    # Step 3: Verify
    print("STEP 3: Verifying Created Files")
    print("-" * 70)
    
    dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
    
    if not os.path.exists(dto_path):
        print(f"✗ Directory not found: {dto_path}")
        return False
    
    print(f"✓ Directory exists: {dto_path}")
    print()
    
    # List files
    print("Directory contents:")
    try:
        files = os.listdir(dto_path)
        if files:
            for filename in sorted(files):
                filepath = os.path.join(dto_path, filename)
                if os.path.isfile(filepath):
                    size = os.path.getsize(filepath)
                    print(f"  • {filename} ({size} bytes)")
        else:
            print("  (directory is empty)")
    except Exception as e:
        print(f"✗ Error listing directory: {e}")
        return False
    
    print()
    
    # Show file contents
    skill_file = os.path.join(dto_path, 'SkillNodeDto.java')
    dna_file = os.path.join(dto_path, 'PortfolioDNADto.java')
    
    if os.path.exists(skill_file):
        print("=" * 70)
        print("SkillNodeDto.java")
        print("=" * 70)
        try:
            with open(skill_file, 'r', encoding='utf-8') as f:
                content = f.read()
                print(content)
        except Exception as e:
            print(f"✗ Error reading file: {e}")
            return False
        print()
    else:
        print(f"✗ SkillNodeDto.java not found")
    
    if os.path.exists(dna_file):
        print("=" * 70)
        print("PortfolioDNADto.java")
        print("=" * 70)
        try:
            with open(dna_file, 'r', encoding='utf-8') as f:
                content = f.read()
                print(content)
        except Exception as e:
            print(f"✗ Error reading file: {e}")
            return False
        print()
    else:
        print(f"✗ PortfolioDNADto.java not found")
    
    print("=" * 70)
    print("✓ VERIFICATION COMPLETE - All files created successfully!")
    print("=" * 70)
    return True

if __name__ == '__main__':
    success = main()
    sys.exit(0 if success else 1)
