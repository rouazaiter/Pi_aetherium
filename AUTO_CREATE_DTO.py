#!/usr/bin/env python3
"""
DTO CREATION - FINAL COMPREHENSIVE SCRIPT
==========================================

This script creates:
1. Directory: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
2. File: SkillNodeDto.java with Lombok annotations
3. File: PortfolioDNADto.java with Lombok annotations

Execution Instructions:
- Run: python C:\Users\jihen\Downloads\portfolio\create_dto_final.py
- Or: python standalone_dto_creator.py
- Or: python test_and_execute_dto.py
- Or: python create_dto_and_verify.py
- Or: python FINAL_DTO_CREATION.py

This script will:
✓ Create the dto directory (if it doesn't exist)
✓ Create both DTO files with correct Lombok annotations
✓ Verify directory and files exist
✓ Display complete file contents
✓ Output execution summary
"""

import os
import sys
import pathlib

def create_dto_directory_and_files():
    """Main function to create DTO directory and files."""
    
    # Define target paths
    base_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
    dto_dir = os.path.join(base_dir, "dto")
    
    print("="*70)
    print("DTO DIRECTORY AND FILES CREATION")
    print("="*70)
    print(f"\nTarget Directory: {dto_dir}\n")
    
    # Create directory
    try:
        os.makedirs(dto_dir, exist_ok=True)
        print(f"✓ Directory created: {dto_dir}")
    except Exception as e:
        print(f"✗ ERROR creating directory: {e}")
        return False
    
    # SkillNodeDto.java content
    skill_dto_content = '''package jihen.portfolio.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SkillNodeDto {
    private Long id;
    private String name;
    private String category;
    private Boolean isTrendy;
    private String description;
    private Integer searchCount;
}
'''
    
    # PortfolioDNADto.java content
    portfolio_dna_dto_content = '''package jihen.portfolio.dto;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioDNADto {
    private Long portfolioId;
    private String title;
    private String bio;
    private Integer totalViews;
    private Integer followerCount;
    private Boolean isVerified;
    private Set<SkillNodeDto> skills;
    private Set<String> projectCategories;
    private String primaryFocus;
    private String expertise;
}
'''
    
    # Create SkillNodeDto.java
    skill_node_path = os.path.join(dto_dir, "SkillNodeDto.java")
    try:
        with open(skill_node_path, 'w', encoding='utf-8') as f:
            f.write(skill_dto_content)
        print(f"✓ File created: {skill_node_path}")
    except Exception as e:
        print(f"✗ ERROR creating SkillNodeDto.java: {e}")
        return False
    
    # Create PortfolioDNADto.java
    portfolio_dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
    try:
        with open(portfolio_dna_path, 'w', encoding='utf-8') as f:
            f.write(portfolio_dna_dto_content)
        print(f"✓ File created: {portfolio_dna_path}")
    except Exception as e:
        print(f"✗ ERROR creating PortfolioDNADto.java: {e}")
        return False
    
    # Verification
    print("\n" + "="*70)
    print("VERIFICATION")
    print("="*70)
    
    # Verify directory
    if not os.path.isdir(dto_dir):
        print("✗ ERROR: Directory does not exist!")
        return False
    print(f"✓ Directory exists: {dto_dir}")
    
    # Verify files
    if not os.path.isfile(skill_node_path):
        print("✗ ERROR: SkillNodeDto.java does not exist!")
        return False
    print(f"✓ SkillNodeDto.java exists")
    
    if not os.path.isfile(portfolio_dna_path):
        print("✗ ERROR: PortfolioDNADto.java does not exist!")
        return False
    print(f"✓ PortfolioDNADto.java exists")
    
    # List directory contents
    print(f"\n✓ Files in directory:")
    files = os.listdir(dto_dir)
    for filename in sorted(files):
        filepath = os.path.join(dto_dir, filename)
        size = os.path.getsize(filepath)
        print(f"  - {filename} ({size} bytes)")
    
    # Display complete file contents
    print("\n" + "="*70)
    print("SkillNodeDto.java Content")
    print("="*70)
    with open(skill_node_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n" + "="*70)
    print("PortfolioDNADto.java Content")
    print("="*70)
    with open(portfolio_dna_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n" + "="*70)
    print("✓ TASK COMPLETED SUCCESSFULLY!")
    print("="*70)
    
    return True

if __name__ == "__main__":
    try:
        success = create_dto_directory_and_files()
        sys.exit(0 if success else 1)
    except Exception as e:
        print(f"\n✗ FATAL ERROR: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
