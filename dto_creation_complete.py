#!/usr/bin/env python3
"""
DTO Creation Script - Creates the dto directory and Java DTO files
This script handles directory creation and file generation for portfolio DTOs
"""

import os
import sys
import shutil
from pathlib import Path

def main():
    # Full path to the DTO directory (singular - dto, not dtos)
    dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
    log_lines = []
    
    try:
        # Log start
        log_lines.append("=" * 70)
        log_lines.append("DTO CREATION SCRIPT - EXECUTION LOG")
        log_lines.append("=" * 70)
        log_lines.append("")
        
        # Remove existing directory if it exists
        if os.path.exists(dto_dir):
            shutil.rmtree(dto_dir)
            msg = f"Removed existing directory: {dto_dir}"
            log_lines.append(msg)
            print(msg)
        
        # Create the directory
        os.makedirs(dto_dir, exist_ok=True)
        msg = f"✓ Directory created: {dto_dir}\n"
        log_lines.append(msg)
        print(msg)
        
        # SkillNodeDto.java content
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
        with open(skill_path, 'w', encoding='utf-8') as f:
            f.write(skill_content)
        msg = f"✓ Created: {skill_path}"
        log_lines.append(msg)
        print(msg)
        
        # PortfolioDNADto.java content
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
        with open(dna_path, 'w', encoding='utf-8') as f:
            f.write(dna_content)
        msg = f"✓ Created: {dna_path}\n"
        log_lines.append(msg)
        print(msg)
        
        # Verification section
        log_lines.append("=" * 70)
        log_lines.append("VERIFICATION")
        log_lines.append("=" * 70)
        
        dir_exists = os.path.isdir(dto_dir)
        skill_exists = os.path.isfile(skill_path)
        dna_exists = os.path.isfile(dna_path)
        
        msgs = [
            f"Directory exists: {dir_exists}",
            f"SkillNodeDto.java exists: {skill_exists}",
            f"PortfolioDNADto.java exists: {dna_exists}",
            ""
        ]
        
        for msg in msgs:
            log_lines.append(msg)
            print(msg)
        
        msg = "Directory contents:"
        log_lines.append(msg)
        print(msg)
        
        for filename in sorted(os.listdir(dto_dir)):
            fpath = os.path.join(dto_dir, filename)
            size = os.path.getsize(fpath)
            msg = f"  ✓ {filename} ({size} bytes)"
            log_lines.append(msg)
            print(msg)
        
        # Display complete file contents
        log_lines.append("\n" + "=" * 70)
        log_lines.append("FILE CONTENTS: SkillNodeDto.java")
        log_lines.append("=" * 70)
        with open(skill_path, 'r', encoding='utf-8') as f:
            content = f.read()
            log_lines.append(content)
            print(content)
        
        log_lines.append("\n" + "=" * 70)
        log_lines.append("FILE CONTENTS: PortfolioDNADto.java")
        log_lines.append("=" * 70)
        with open(dna_path, 'r', encoding='utf-8') as f:
            content = f.read()
            log_lines.append(content)
            print(content)
        
        log_lines.append("\n" + "=" * 70)
        log_lines.append("✓✓✓ SUCCESS - All files created and verified! ✓✓✓")
        log_lines.append("=" * 70)
        print("\n" + "=" * 70)
        print("✓✓✓ SUCCESS - All files created and verified! ✓✓✓")
        print("=" * 70)
        
        # Write log file
        log_path = r"C:\Users\jihen\Downloads\portfolio\dto_creation_log.txt"
        with open(log_path, 'w', encoding='utf-8') as f:
            f.write("\n".join(log_lines))
        
        print(f"\nLog written to: {log_path}")
        return 0
        
    except Exception as e:
        error_msg = f"ERROR: {e}"
        log_lines.append(error_msg)
        print(error_msg)
        import traceback
        traceback.print_exc()
        
        # Write error log
        log_path = r"C:\Users\jihen\Downloads\portfolio\dto_creation_log.txt"
        with open(log_path, 'w', encoding='utf-8') as f:
            log_lines.append("\nTraceback:")
            log_lines.append(traceback.format_exc())
            f.write("\n".join(log_lines))
        
        return 1

if __name__ == "__main__":
    sys.exit(main())
