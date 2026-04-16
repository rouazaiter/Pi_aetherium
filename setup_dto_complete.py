#!/usr/bin/env python3
"""
Comprehensive DTO Setup Script
This script creates the dto directory and Java DTO files
"""
import os
import sys
import subprocess
from pathlib import Path

def create_with_python():
    """Create using Python pathlib and file operations"""
    print("=" * 60)
    print("Creating DTO files using Python...")
    print("=" * 60)
    
    dir_path = Path(r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto")
    
    try:
        # Create directory
        dir_path.mkdir(parents=True, exist_ok=True)
        print(f"✓ Created directory: {dir_path}")
        
        # Create SkillNodeDto.java
        skill_node_content = '''package jihen.portfolio.dto;

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
        
        skill_node_file = dir_path / "SkillNodeDto.java"
        skill_node_file.write_text(skill_node_content)
        print(f"✓ Created: {skill_node_file}")
        
        # Create PortfolioDNADto.java
        portfolio_dna_content = '''package jihen.portfolio.dto;

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
        
        portfolio_dna_file = dir_path / "PortfolioDNADto.java"
        portfolio_dna_file.write_text(portfolio_dna_content)
        print(f"✓ Created: {portfolio_dna_file}")
        
        # Verify
        print("\nVerification:")
        files = list(dir_path.glob("*.java"))
        for f in files:
            print(f"  ✓ {f.name}")
        
        if len(files) == 2:
            print("\n" + "=" * 60)
            print("SUCCESS! All DTO files created successfully!")
            print("=" * 60)
            return True
        else:
            print(f"\nWarning: Expected 2 files, found {len(files)}")
            return False
            
    except Exception as e:
        print(f"\n✗ Error: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = create_with_python()
    sys.exit(0 if success else 1)
