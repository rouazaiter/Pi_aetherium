#!/usr/bin/env python3
"""
Direct DTO File Creation (Workaround for environment limitations)
This creates the DTO files directly without relying on Java compilation
"""

import os
import pathlib
import sys

def create_dto_setup():
    """Create the DTO directory and files"""
    
    # Define paths
    base_path = r'C:\Users\jihen\Downloads\portfolio'
    dto_dir = os.path.join(base_path, r'src\main\java\jihen\portfolio\dto')
    
    print("\n" + "=" * 80)
    print(" " * 25 + "DTO SETUP - DIRECT CREATION")
    print("=" * 80 + "\n")
    
    # Ensure directory exists
    print(f"Creating directory: {dto_dir}")
    try:
        pathlib.Path(dto_dir).mkdir(parents=True, exist_ok=True)
        print("✓ Directory created successfully\n")
    except Exception as e:
        print(f"✗ Error creating directory: {e}\n")
        return False
    
    # SkillNodeDto content
    skill_node_dto = """package jihen.portfolio.dto;

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
    
    # PortfolioDNADto content
    portfolio_dna_dto = """package jihen.portfolio.dto;

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
    
    # Create SkillNodeDto.java
    skill_file = os.path.join(dto_dir, 'SkillNodeDto.java')
    print(f"Creating file: {skill_file}")
    try:
        with open(skill_file, 'w', encoding='utf-8') as f:
            f.write(skill_node_dto)
        print("✓ SkillNodeDto.java created successfully\n")
    except Exception as e:
        print(f"✗ Error creating SkillNodeDto.java: {e}\n")
        return False
    
    # Create PortfolioDNADto.java
    dna_file = os.path.join(dto_dir, 'PortfolioDNADto.java')
    print(f"Creating file: {dna_file}")
    try:
        with open(dna_file, 'w', encoding='utf-8') as f:
            f.write(portfolio_dna_dto)
        print("✓ PortfolioDNADto.java created successfully\n")
    except Exception as e:
        print(f"✗ Error creating PortfolioDNADto.java: {e}\n")
        return False
    
    # Verification
    print("=" * 80)
    print("VERIFICATION")
    print("=" * 80 + "\n")
    
    print(f"✓ Created directory: {dto_dir}")
    print(f"✓ Created SkillNodeDto.java")
    print(f"✓ Created PortfolioDNADto.java")
    
    print("\n✓ Files created successfully!")
    print(f"Directory: {dto_dir}")
    print("Files:")
    
    try:
        for filename in sorted(os.listdir(dto_dir)):
            filepath = os.path.join(dto_dir, filename)
            if os.path.isfile(filepath):
                size = os.path.getsize(filepath)
                print(f"  - {filename} ({size} bytes)")
    except Exception as e:
        print(f"✗ Error listing directory: {e}\n")
        return False
    
    # Show file contents
    print("\n" + "=" * 80)
    print("SkillNodeDto.java")
    print("=" * 80)
    try:
        with open(skill_file, 'r', encoding='utf-8') as f:
            print(f.read())
    except Exception as e:
        print(f"✗ Error reading file: {e}")
        return False
    
    print("\n" + "=" * 80)
    print("PortfolioDNADto.java")
    print("=" * 80)
    try:
        with open(dna_file, 'r', encoding='utf-8') as f:
            print(f.read())
    except Exception as e:
        print(f"✗ Error reading file: {e}")
        return False
    
    print("\n" + "=" * 80)
    print("✓ DTO SETUP COMPLETE - All files created successfully!")
    print("=" * 80 + "\n")
    
    return True

if __name__ == '__main__':
    success = create_dto_setup()
    sys.exit(0 if success else 1)
