#!/usr/bin/env python3
"""
Create the DTO directory structure with exact files as requested
"""
import os
import sys
from pathlib import Path

def create_dto_structure():
    # Create the dto directory (singular, as requested)
    dto_dir = Path(r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto')
    
    print(f"Creating directory: {dto_dir}")
    dto_dir.mkdir(parents=True, exist_ok=True)
    print(f"✓ Directory created: {dto_dir}\n")
    
    # SkillNodeDto.java - EXACT CONTENT
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
    
    skill_node_path = dto_dir / 'SkillNodeDto.java'
    skill_node_path.write_text(skill_node_dto)
    print(f"✓ Created: {skill_node_path}")
    
    # PortfolioDNADto.java - EXACT CONTENT
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
    
    portfolio_dna_path = dto_dir / 'PortfolioDNADto.java'
    portfolio_dna_path.write_text(portfolio_dna_dto)
    print(f"✓ Created: {portfolio_dna_path}\n")
    
    # Verify and display
    print("=" * 80)
    print("DIRECTORY LISTING")
    print("=" * 80)
    print(f"Contents of {dto_dir}:")
    for file in sorted(dto_dir.glob('*.java')):
        print(f"  - {file.name}")
    
    print("\n" + "=" * 80)
    print("FILE VERIFICATION")
    print("=" * 80)
    
    print(f"\n✓ SkillNodeDto.java ({skill_node_path.stat().st_size} bytes)")
    print("-" * 80)
    print(skill_node_path.read_text())
    
    print(f"\n✓ PortfolioDNADto.java ({portfolio_dna_path.stat().st_size} bytes)")
    print("-" * 80)
    print(portfolio_dna_path.read_text())
    
    print("\n" + "=" * 80)
    print("✅ ALL FILES CREATED AND VERIFIED SUCCESSFULLY")
    print("=" * 80)

if __name__ == '__main__':
    try:
        create_dto_structure()
        sys.exit(0)
    except Exception as e:
        print(f"❌ Error: {e}", file=sys.stderr)
        sys.exit(1)
