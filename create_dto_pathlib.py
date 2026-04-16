#!/usr/bin/env python3
"""
DTO Creation Script using Pathlib
Run: python C:\Users\jihen\Downloads\portfolio\create_dto_pathlib.py
"""

from pathlib import Path
import sys

def main():
    # Define paths using pathlib
    base_path = Path(r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio")
    dto_dir = base_path / "dto"
    
    print("="*70)
    print("DTO DIRECTORY AND FILES CREATION (PATHLIB VERSION)")
    print("="*70)
    print(f"\nTarget Directory: {dto_dir}\n")
    
    # Create directory
    try:
        dto_dir.mkdir(parents=True, exist_ok=True)
        print(f"✓ Directory created: {dto_dir}")
    except Exception as e:
        print(f"✗ ERROR creating directory: {e}")
        return False
    
    # File contents
    skill_dto = '''package jihen.portfolio.dto;

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
    
    # Create SkillNodeDto.java
    skill_file = dto_dir / "SkillNodeDto.java"
    try:
        skill_file.write_text(skill_dto, encoding='utf-8')
        print(f"✓ File created: {skill_file}")
    except Exception as e:
        print(f"✗ ERROR creating SkillNodeDto.java: {e}")
        return False
    
    # Create PortfolioDNADto.java
    dna_file = dto_dir / "PortfolioDNADto.java"
    try:
        dna_file.write_text(portfolio_dna_dto, encoding='utf-8')
        print(f"✓ File created: {dna_file}")
    except Exception as e:
        print(f"✗ ERROR creating PortfolioDNADto.java: {e}")
        return False
    
    # Verification
    print("\n" + "="*70)
    print("VERIFICATION")
    print("="*70)
    
    print(f"✓ Directory exists: {dto_dir.is_dir()}")
    print(f"✓ SkillNodeDto.java exists: {skill_file.is_file()}")
    print(f"✓ PortfolioDNADto.java exists: {dna_file.is_file()}")
    
    # List directory contents
    print(f"\n✓ Files in directory:")
    for file in sorted(dto_dir.iterdir()):
        size = file.stat().st_size
        print(f"  - {file.name} ({size} bytes)")
    
    # Display contents
    print("\n" + "="*70)
    print("SkillNodeDto.java Content")
    print("="*70)
    print(skill_file.read_text(encoding='utf-8'))
    
    print("\n" + "="*70)
    print("PortfolioDNADto.java Content")
    print("="*70)
    print(dna_file.read_text(encoding='utf-8'))
    
    print("\n" + "="*70)
    print("✓ TASK COMPLETED SUCCESSFULLY!")
    print("="*70)
    
    return True

if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except Exception as e:
        print(f"\n✗ FATAL ERROR: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
