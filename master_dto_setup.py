#!/usr/bin/env python3
"""
MASTER DTO SETUP SCRIPT
========================
This script creates the dto directory and all required DTO Java files.
Run this script from the portfolio root directory.

Usage:
    python master_dto_setup.py
    python3 master_dto_setup.py
"""

import os
import sys
from pathlib import Path

def main():
    """Main setup function"""
    
    # Target directory
    dto_dir = Path(r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto")
    
    print("\n" + "="*70)
    print("GITHUB COPILOT DTO SETUP")
    print("="*70)
    print(f"\nTarget directory: {dto_dir}")
    
    # Create directory
    try:
        dto_dir.mkdir(parents=True, exist_ok=True)
        print(f"✓ Directory created/verified: {dto_dir}")
    except Exception as e:
        print(f"✗ Failed to create directory: {e}")
        return False
    
    # SkillNodeDto.java
    skill_node_dto = '''package jihen.portfolio.dto;

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
    
    # PortfolioDNADto.java
    portfolio_dna_dto = '''package jihen.portfolio.dto;

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
    
    files_created = []
    
    # Create SkillNodeDto.java
    try:
        skill_file = dto_dir / "SkillNodeDto.java"
        skill_file.write_text(skill_node_dto, encoding='utf-8')
        print(f"✓ Created: {skill_file.name}")
        files_created.append(str(skill_file))
    except Exception as e:
        print(f"✗ Failed to create SkillNodeDto.java: {e}")
        return False
    
    # Create PortfolioDNADto.java
    try:
        dna_file = dto_dir / "PortfolioDNADto.java"
        dna_file.write_text(portfolio_dna_dto, encoding='utf-8')
        print(f"✓ Created: {dna_file.name}")
        files_created.append(str(dna_file))
    except Exception as e:
        print(f"✗ Failed to create PortfolioDNADto.java: {e}")
        return False
    
    # Verification
    print("\n" + "-"*70)
    print("VERIFICATION")
    print("-"*70)
    
    java_files = list(dto_dir.glob("*.java"))
    print(f"\nFiles in {dto_dir.name}/ directory:")
    for java_file in sorted(java_files):
        size = java_file.stat().st_size
        print(f"  ✓ {java_file.name} ({size} bytes)")
    
    if len(java_files) >= 2:
        print("\n" + "="*70)
        print("SUCCESS! All DTO files created successfully!")
        print("="*70)
        print(f"\nLocation: {dto_dir}")
        print(f"\nFiles created:")
        print(f"  1. SkillNodeDto.java")
        print(f"  2. PortfolioDNADto.java")
        print("\nThe DTO files are ready for use in your Spring Boot project.")
        print("="*70 + "\n")
        return True
    else:
        print(f"\n✗ Expected at least 2 Java files, found {len(java_files)}")
        return False

if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\nSetup cancelled by user.")
        sys.exit(1)
    except Exception as e:
        print(f"\n✗ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
