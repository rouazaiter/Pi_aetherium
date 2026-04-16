#!/usr/bin/env python3
"""
AUTO-EXECUTE DTO SETUP
This script will compile and run SetupDTOFiles.java and display all results
"""

import subprocess
import os
import sys

def main():
    os.chdir(r'C:\Users\jihen\Downloads\portfolio')
    
    print("\n" + "█" * 80)
    print("█" + " " * 78 + "█")
    print("█" + " " * 15 + "DTO SETUP - AUTO EXECUTION AND VERIFICATION" + " " * 22 + "█")
    print("█" + " " * 78 + "█")
    print("█" * 80 + "\n")
    
    # STEP 1: Compile
    print("STEP 1: COMPILING SetupDTOFiles.java")
    print("-" * 80)
    result = subprocess.run(['javac', 'SetupDTOFiles.java'], capture_output=True, text=True, timeout=30)
    
    if result.returncode == 0:
        print("✓ Compilation successful\n")
    else:
        print("✗ COMPILATION FAILED!")
        print("STDOUT:", result.stdout)
        print("STDERR:", result.stderr)
        return False
    
    # STEP 2: Execute
    print("STEP 2: RUNNING SetupDTOFiles")
    print("-" * 80)
    result = subprocess.run(['java', 'SetupDTOFiles'], capture_output=True, text=True, timeout=30)
    
    if result.returncode == 0:
        print(result.stdout)
    else:
        print("✗ EXECUTION FAILED!")
        print("STDOUT:", result.stdout)
        print("STDERR:", result.stderr)
        return False
    
    # STEP 3: Verify
    print("STEP 3: VERIFYING CREATED FILES")
    print("-" * 80)
    
    dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
    
    if not os.path.exists(dto_dir):
        print(f"✗ ERROR: Directory not created: {dto_dir}")
        return False
    
    print(f"✓ Directory exists: {dto_dir}\n")
    
    # List directory
    print("Directory contents:")
    files = sorted(os.listdir(dto_dir))
    if not files:
        print("  (empty)")
        return False
    
    for filename in files:
        filepath = os.path.join(dto_dir, filename)
        if os.path.isfile(filepath):
            size = os.path.getsize(filepath)
            print(f"  • {filename:30} ({size:4} bytes)")
    
    print()
    
    # Display file 1
    skill_file = os.path.join(dto_dir, 'SkillNodeDto.java')
    if os.path.exists(skill_file):
        print("=" * 80)
        print("SkillNodeDto.java")
        print("=" * 80)
        with open(skill_file, 'r', encoding='utf-8') as f:
            print(f.read())
    else:
        print(f"✗ ERROR: {skill_file} not found")
        return False
    
    # Display file 2
    dna_file = os.path.join(dto_dir, 'PortfolioDNADto.java')
    if os.path.exists(dna_file):
        print("\n" + "=" * 80)
        print("PortfolioDNADto.java")
        print("=" * 80)
        with open(dna_file, 'r', encoding='utf-8') as f:
            print(f.read())
    else:
        print(f"✗ ERROR: {dna_file} not found")
        return False
    
    # Summary
    print("\n" + "█" * 80)
    print("█" + " " * 78 + "█")
    print("█" + " " * 20 + "✓ DTO SETUP COMPLETE - ALL FILES CREATED SUCCESSFULLY!" + " " * 8 + "█")
    print("█" + " " * 78 + "█")
    print("█" * 80)
    
    print("\nSummary:")
    print(f"  Location: {dto_dir}")
    print(f"  Files created: {len([f for f in files if os.path.isfile(os.path.join(dto_dir, f))])}")
    print("  ✓ SkillNodeDto.java")
    print("  ✓ PortfolioDNADto.java")
    print("\n")
    
    return True

if __name__ == '__main__':
    try:
        success = main()
        sys.exit(0 if success else 1)
    except Exception as e:
        print(f"\n✗ ERROR: {e}")
        sys.exit(1)
