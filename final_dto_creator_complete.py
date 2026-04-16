#!/usr/bin/env python3
"""
Final DTO Creator - Complete Implementation
This script performs all DTO creation operations and outputs results
"""

import os
import sys
import traceback

def main():
    try:
        # Change to portfolio directory
        portfolio_dir = r"C:\Users\jihen\Downloads\portfolio"
        os.chdir(portfolio_dir)
        print(f"Working directory: {os.getcwd()}")
        print()
        
        # Define paths
        base_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
        dto_dir = os.path.join(base_dir, "dto")
        
        # Step 1: Create directory
        print(f"Creating directory: {dto_dir}")
        try:
            os.makedirs(dto_dir, exist_ok=True)
            print(f"✓ Directory created: {dto_dir}")
        except Exception as e:
            print(f"✗ ERROR creating directory: {e}")
            raise
        
        print()
        
        # Step 2: Create SkillNodeDto.java
        skill_content = """package jihen.portfolio.dto;

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
"""
        
        skill_path = os.path.join(dto_dir, "SkillNodeDto.java")
        print(f"Creating: {skill_path}")
        with open(skill_path, 'w', encoding='utf-8') as f:
            f.write(skill_content)
        print(f"✓ File created: {skill_path}")
        
        print()
        
        # Step 3: Create PortfolioDNADto.java
        dna_content = """package jihen.portfolio.dto;

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
"""
        
        dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
        print(f"Creating: {dna_path}")
        with open(dna_path, 'w', encoding='utf-8') as f:
            f.write(dna_content)
        print(f"✓ File created: {dna_path}")
        
        print()
        print("=" * 70)
        print("VERIFICATION")
        print("=" * 70)
        print()
        
        # Step 4: Verify directory exists
        if not os.path.isdir(dto_dir):
            print(f"✗ ERROR: Directory does not exist!")
            return False
        print(f"✓ Directory exists: {dto_dir}")
        
        # Step 5: Verify files exist
        if not os.path.isfile(skill_path):
            print(f"✗ ERROR: SkillNodeDto.java does not exist!")
            return False
        print(f"✓ SkillNodeDto.java exists")
        
        if not os.path.isfile(dna_path):
            print(f"✗ ERROR: PortfolioDNADto.java does not exist!")
            return False
        print(f"✓ PortfolioDNADto.java exists")
        
        # Step 6: List directory contents
        print()
        files = os.listdir(dto_dir)
        print(f"✓ Files in directory: {len(files)}")
        for f in sorted(files):
            fpath = os.path.join(dto_dir, f)
            size = os.path.getsize(fpath)
            print(f"  - {f} ({size} bytes)")
        
        # Step 7: Display file contents
        print()
        print("=" * 70)
        print("SkillNodeDto.java Content")
        print("=" * 70)
        with open(skill_path, 'r', encoding='utf-8') as f:
            content = f.read()
            print(content)
        
        print()
        print("=" * 70)
        print("PortfolioDNADto.java Content")
        print("=" * 70)
        with open(dna_path, 'r', encoding='utf-8') as f:
            content = f.read()
            print(content)
        
        print()
        print("=" * 70)
        print("✓ TASK COMPLETED SUCCESSFULLY!")
        print("=" * 70)
        
        return True
        
    except Exception as e:
        print(f"✗ ERROR: {e}")
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
