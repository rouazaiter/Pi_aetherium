#!/usr/bin/env python3
"""
ULTIMATE DTO CREATION SCRIPT
=============================
This is the definitive script that creates the DTO directory and files.
It uses pathlib.Path and os module for maximum compatibility.
Run from: C:\Users\jihen\Downloads\portfolio
Command: python ultimate_dto_creation.py OR python3 ultimate_dto_creation.py
"""

import os
import sys
from pathlib import Path

def create_dto_structure():
    """Create the DTO directory structure and Java files."""
    
    output = []
    
    # Define paths
    portfolio_root = Path(r"C:\Users\jihen\Downloads\portfolio")
    dto_dir = portfolio_root / "src" / "main" / "java" / "jihen" / "portfolio" / "dto"
    
    try:
        # Print header
        header = "=" * 80
        output.append(header)
        output.append("DTO CREATION EXECUTION - ULTIMATE SCRIPT".center(80))
        output.append(header)
        output.append("")
        
        # Step 1: Remove existing if it exists
        if dto_dir.exists():
            import shutil
            shutil.rmtree(dto_dir)
            msg = f"Removed existing directory: {dto_dir}"
            output.append(msg)
            print(msg)
        
        # Step 2: Create the directory
        dto_dir.mkdir(parents=True, exist_ok=True)
        msg = f"✓ Created directory: {dto_dir}\n"
        output.append(msg)
        print(msg)
        
        # Step 3: Create SkillNodeDto.java
        skill_dto_content = """package jihen.portfolio.dto;

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
        skill_dto_path = dto_dir / "SkillNodeDto.java"
        skill_dto_path.write_text(skill_dto_content, encoding='utf-8')
        msg = f"✓ Created: SkillNodeDto.java ({skill_dto_path.stat().st_size} bytes)"
        output.append(msg)
        print(msg)
        
        # Step 4: Create PortfolioDNADto.java
        dna_dto_content = """package jihen.portfolio.dto;

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
        dna_dto_path = dto_dir / "PortfolioDNADto.java"
        dna_dto_path.write_text(dna_dto_content, encoding='utf-8')
        msg = f"✓ Created: PortfolioDNADto.java ({dna_dto_path.stat().st_size} bytes)\n"
        output.append(msg)
        print(msg)
        
        # Step 5: Verification
        divider = "=" * 80
        output.append(divider)
        output.append("VERIFICATION".center(80))
        output.append(divider)
        print(divider)
        print("VERIFICATION".center(80))
        print(divider)
        
        dir_exists = dto_dir.is_dir()
        skill_exists = skill_dto_path.is_file()
        dna_exists = dna_dto_path.is_file()
        
        verification_lines = [
            f"Directory exists: {dir_exists}",
            f"SkillNodeDto.java exists: {skill_exists}",
            f"PortfolioDNADto.java exists: {dna_exists}",
            ""
        ]
        
        for line in verification_lines:
            output.append(line)
            print(line)
        
        # Directory contents
        output.append("Directory contents:")
        print("Directory contents:")
        for file_path in sorted(dto_dir.iterdir()):
            file_size = file_path.stat().st_size
            msg = f"  ✓ {file_path.name} ({file_size} bytes)"
            output.append(msg)
            print(msg)
        
        # Step 6: Display file contents
        output.append("\n" + "=" * 80)
        output.append("FILE CONTENTS: SkillNodeDto.java".center(80))
        output.append("=" * 80)
        print("\n" + "=" * 80)
        print("FILE CONTENTS: SkillNodeDto.java".center(80))
        print("=" * 80)
        
        skill_content = skill_dto_path.read_text(encoding='utf-8')
        output.append(skill_content)
        print(skill_content)
        
        output.append("\n" + "=" * 80)
        output.append("FILE CONTENTS: PortfolioDNADto.java".center(80))
        output.append("=" * 80)
        print("\n" + "=" * 80)
        print("FILE CONTENTS: PortfolioDNADto.java".center(80))
        print("=" * 80)
        
        dna_content = dna_dto_path.read_text(encoding='utf-8')
        output.append(dna_content)
        print(dna_content)
        
        # Success message
        output.append("\n" + "=" * 80)
        output.append("✓✓✓ SUCCESS - All files created and verified! ✓✓✓".center(80))
        output.append("=" * 80)
        print("\n" + "=" * 80)
        print("✓✓✓ SUCCESS - All files created and verified! ✓✓✓".center(80))
        print("=" * 80)
        
        # Save log
        log_file = portfolio_root / "ultimate_dto_creation.log"
        log_file.write_text("\n".join(output), encoding='utf-8')
        print(f"\nLog saved to: {log_file}")
        
        return 0
        
    except Exception as e:
        error_msg = f"ERROR: {e}"
        output.append(error_msg)
        print(error_msg)
        
        import traceback
        traceback.print_exc()
        output.append("\nTraceback:")
        output.append(traceback.format_exc())
        
        # Save error log
        log_file = portfolio_root / "ultimate_dto_creation.log"
        log_file.write_text("\n".join(output), encoding='utf-8')
        
        return 1

if __name__ == "__main__":
    sys.exit(create_dto_structure())
