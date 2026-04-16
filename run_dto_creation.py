#!/usr/bin/env python3
"""
Direct DTO Creation Script - No dependencies
Creates the dto directory and files without relying on external tools
"""
import os
import sys
import shutil
from pathlib import Path

def main():
    try:
        print("=" * 70)
        print("DTO CREATION SCRIPT - DIRECT EXECUTION")
        print("=" * 70)
        print()
        
        # Define paths
        dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
        
        # Create directory
        print(f"Creating directory: {dto_dir}")
        Path(dto_dir).mkdir(parents=True, exist_ok=True)
        if os.path.isdir(dto_dir):
            print(f"✓ Directory created successfully")
        else:
            print(f"✗ Failed to create directory")
            return 1
        
        print()
        
        # File 1: SkillNodeDto.java
        skill_node = """package jihen.portfolio.dto;

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
            f.write(skill_node)
        if os.path.isfile(skill_path):
            size = os.path.getsize(skill_path)
            print(f"✓ Created ({size} bytes)")
        else:
            print(f"✗ Failed to create file")
            return 1
        
        print()
        
        # File 2: PortfolioDNADto.java
        portfolio_dna = """package jihen.portfolio.dto;

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
            f.write(portfolio_dna)
        if os.path.isfile(dna_path):
            size = os.path.getsize(dna_path)
            print(f"✓ Created ({size} bytes)")
        else:
            print(f"✗ Failed to create file")
            return 1
        
        print()
        print("=" * 70)
        print("VERIFICATION")
        print("=" * 70)
        print()
        
        print(f"Directory exists: {os.path.isdir(dto_dir)}")
        print(f"SkillNodeDto.java exists: {os.path.isfile(skill_path)}")
        print(f"PortfolioDNADto.java exists: {os.path.isfile(dna_path)}")
        
        print()
        print("Directory contents:")
        for item in os.listdir(dto_dir):
            fpath = os.path.join(dto_dir, item)
            size = os.path.getsize(fpath)
            print(f"  ✓ {item} ({size} bytes)")
        
        print()
        print("=" * 70)
        print("FILE CONTENTS: SkillNodeDto.java")
        print("=" * 70)
        print()
        with open(skill_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print()
        print("=" * 70)
        print("FILE CONTENTS: PortfolioDNADto.java")
        print("=" * 70)
        print()
        with open(dna_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print()
        print("=" * 70)
        print("✓✓✓ SUCCESS - ALL TASKS COMPLETED! ✓✓✓")
        print("=" * 70)
        
        return 0
        
    except Exception as e:
        print(f"\n✗ ERROR: {e}")
        import traceback
        traceback.print_exc()
        return 1

if __name__ == "__main__":
    sys.exit(main())
