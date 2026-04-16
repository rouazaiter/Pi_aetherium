#!/usr/bin/env python3
"""
DTO Directory and Files Creator
This script creates the dto directory and DTO files with full verification output
"""
import os
import sys

def create_dtos():
    base_path = r"C:\Users\jihen\Downloads\portfolio"
    java_path = os.path.join(base_path, "src", "main", "java", "jihen", "portfolio")
    dto_dir = os.path.join(java_path, "dto")
    
    # File contents
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

    try:
        # Create directory
        print(f"Creating directory: {dto_dir}")
        os.makedirs(dto_dir, exist_ok=True)
        print(f"✓ Directory created: {dto_dir}")
        
        # Create SkillNodeDto.java
        skill_file_path = os.path.join(dto_dir, "SkillNodeDto.java")
        print(f"\nCreating file: {skill_file_path}")
        with open(skill_file_path, 'w', encoding='utf-8') as f:
            f.write(skill_dto_content)
        print(f"✓ File created: {skill_file_path}")
        
        # Create PortfolioDNADto.java
        dna_file_path = os.path.join(dto_dir, "PortfolioDNADto.java")
        print(f"\nCreating file: {dna_file_path}")
        with open(dna_file_path, 'w', encoding='utf-8') as f:
            f.write(portfolio_dna_dto_content)
        print(f"✓ File created: {dna_file_path}")
        
        # Verification section
        print("\n" + "="*70)
        print("VERIFICATION")
        print("="*70)
        
        # Check if directory exists
        dir_exists = os.path.isdir(dto_dir)
        print(f"✓ Directory exists: {dir_exists} - {dto_dir}")
        
        if not dir_exists:
            print("✗ ERROR: Directory verification failed!")
            return False
        
        # Check files
        skill_exists = os.path.isfile(skill_file_path)
        dna_exists = os.path.isfile(dna_file_path)
        
        print(f"✓ SkillNodeDto.java exists: {skill_exists}")
        print(f"✓ PortfolioDNADto.java exists: {dna_exists}")
        
        # List directory contents
        print(f"\n✓ Files in directory:")
        files_list = os.listdir(dto_dir)
        for filename in sorted(files_list):
            filepath = os.path.join(dto_dir, filename)
            size = os.path.getsize(filepath)
            print(f"  - {filename} ({size} bytes)")
        
        # Display complete file contents
        print("\n" + "="*70)
        print("SkillNodeDto.java Content")
        print("="*70)
        with open(skill_file_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print("\n" + "="*70)
        print("PortfolioDNADto.java Content")
        print("="*70)
        with open(dna_file_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print("\n" + "="*70)
        print("✓ TASK COMPLETED SUCCESSFULLY!")
        print("="*70)
        
        return True
        
    except Exception as e:
        print(f"\n✗ ERROR: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = create_dtos()
    sys.exit(0 if success else 1)
