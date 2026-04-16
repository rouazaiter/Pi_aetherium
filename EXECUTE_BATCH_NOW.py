#!/usr/bin/env python3
"""
Final execution of batch file equivalent
Creates the directory and files exactly as the batch would
"""

import os
import sys

def main():
    # Navigate to portfolio directory
    portfolio_dir = r'C:\Users\jihen\Downloads\portfolio'
    os.chdir(portfolio_dir)
    
    # Path for DTO directory
    dto_dir = r'src\main\java\jihen\portfolio\dto'
    full_dto_path = os.path.join(portfolio_dir, dto_dir)
    
    # Output buffer
    output_lines = []
    
    output_lines.append("=" * 80)
    output_lines.append("BATCH FILE EXECUTION: create_dto_batch_now.bat")
    output_lines.append("=" * 80)
    output_lines.append("")
    
    # Step 1: Create directory
    output_lines.append("Creating dto directory...")
    try:
        os.makedirs(full_dto_path, exist_ok=True)
        if os.path.exists(full_dto_path):
            output_lines.append(f"Directory created successfully: {dto_dir}")
        else:
            output_lines.append("Failed to create directory")
            return 1
    except Exception as e:
        output_lines.append(f"Error creating directory: {e}")
        return 1
    
    # SkillNodeDto content
    skillnodedto_content = """package jihen.portfolio.dto;

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
}"""
    
    # PortfolioDNADto content
    portfoliodnadto_content = """package jihen.portfolio.dto;

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
}"""
    
    # Step 2: Create SkillNodeDto.java
    output_lines.append("Creating SkillNodeDto.java...")
    skillnodedto_path = os.path.join(full_dto_path, 'SkillNodeDto.java')
    try:
        with open(skillnodedto_path, 'w') as f:
            f.write(skillnodedto_content)
        output_lines.append("SkillNodeDto.java created successfully")
    except Exception as e:
        output_lines.append(f"Error creating SkillNodeDto.java: {e}")
        return 1
    
    # Step 3: Create PortfolioDNADto.java
    output_lines.append("Creating PortfolioDNADto.java...")
    portfoliodnadto_path = os.path.join(full_dto_path, 'PortfolioDNADto.java')
    try:
        with open(portfoliodnadto_path, 'w') as f:
            f.write(portfoliodnadto_content)
        output_lines.append("PortfolioDNADto.java created successfully")
    except Exception as e:
        output_lines.append(f"Error creating PortfolioDNADto.java: {e}")
        return 1
    
    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("VERIFICATION")
    output_lines.append("=" * 80)
    output_lines.append("")
    
    # Verification
    if os.path.exists(skillnodedto_path):
        output_lines.append("SkillNodeDto.java created successfully")
    else:
        output_lines.append("FAILED: SkillNodeDto.java not found")
    
    if os.path.exists(portfoliodnadto_path):
        output_lines.append("PortfolioDNADto.java created successfully")
    else:
        output_lines.append("FAILED: PortfolioDNADto.java not found")
    
    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("SkillNodeDto.java CONTENT")
    output_lines.append("=" * 80)
    output_lines.append("")
    output_lines.append(skillnodedto_content)
    
    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("PortfolioDNADto.java CONTENT")
    output_lines.append("=" * 80)
    output_lines.append("")
    output_lines.append(portfoliodnadto_content)
    
    output_lines.append("")
    output_lines.append("=" * 80)
    output_lines.append("TASK COMPLETED")
    output_lines.append("=" * 80)
    
    # Print to stdout
    full_output = '\n'.join(output_lines)
    print(full_output)
    
    # Save to file for persistence
    output_file = os.path.join(portfolio_dir, 'BATCH_EXECUTION_OUTPUT.txt')
    with open(output_file, 'w') as f:
        f.write(full_output)
    
    print(f"\n[Output also saved to: {output_file}]")
    
    return 0

if __name__ == '__main__':
    sys.exit(main())
