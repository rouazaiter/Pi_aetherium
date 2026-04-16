#!/usr/bin/env python3
"""
Complete DTO Creation with Output Capture
This creates the dto directory and both Java DTO files
"""
import os
import sys

def create_dtos():
    # Setup
    os.chdir(r"C:\Users\jihen\Downloads\portfolio")
    base_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
    dto_dir = os.path.join(base_dir, "dto")
    
    # Content
    skill_content = '''package jihen.portfolio.dto;

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
    
    dna_content = '''package jihen.portfolio.dto;

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
    
    print("="*70)
    print("DTO CREATION SCRIPT")
    print("="*70)
    print()
    
    # Create directories
    try:
        os.makedirs(dto_dir, exist_ok=True)
        print(f"✓ Directory created: {dto_dir}")
    except Exception as e:
        print(f"✗ ERROR: {e}")
        return False
    
    # Create files
    skill_path = os.path.join(dto_dir, "SkillNodeDto.java")
    dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
    
    try:
        with open(skill_path, 'w', encoding='utf-8') as f:
            f.write(skill_content)
        print(f"✓ File created: {skill_path}")
    except Exception as e:
        print(f"✗ ERROR: {e}")
        return False
    
    try:
        with open(dna_path, 'w', encoding='utf-8') as f:
            f.write(dna_content)
        print(f"✓ File created: {dna_path}")
    except Exception as e:
        print(f"✗ ERROR: {e}")
        return False
    
    print()
    print("="*70)
    print("VERIFICATION")
    print("="*70)
    print()
    
    # Verify
    if not os.path.isdir(dto_dir):
        print("✗ ERROR: Directory does not exist!")
        return False
    print(f"✓ Directory exists: {dto_dir}")
    
    files = os.listdir(dto_dir)
    print(f"✓ Files in directory: {len(files)}")
    for f in sorted(files):
        fpath = os.path.join(dto_dir, f)
        size = os.path.getsize(fpath)
        print(f"  - {f} ({size} bytes)")
    
    print()
    print("="*70)
    print("SkillNodeDto.java Content")
    print("="*70)
    print()
    try:
        with open(skill_path, 'r', encoding='utf-8') as f:
            print(f.read())
    except Exception as e:
        print(f"ERROR: {e}")
        return False
    
    print()
    print("="*70)
    print("PortfolioDNADto.java Content")
    print("="*70)
    print()
    try:
        with open(dna_path, 'r', encoding='utf-8') as f:
            print(f.read())
    except Exception as e:
        print(f"ERROR: {e}")
        return False
    
    print()
    print("="*70)
    print("✓ TASK COMPLETED SUCCESSFULLY!")
    print("="*70)
    
    return True

if __name__ == "__main__":
    success = create_dtos()
    sys.exit(0 if success else 1)
