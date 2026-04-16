#!/usr/bin/env python3
import os
import sys

def create_dto_files():
    base_path = r'C:\Users\jihen\Downloads\portfolio'
    dto_dir = os.path.join(base_path, r'src\main\java\jihen\portfolio\dto')
    
    print("=" * 60)
    print("Creating dto directory...")
    print("=" * 60)
    
    # Create the directory
    os.makedirs(dto_dir, exist_ok=True)
    
    if os.path.exists(dto_dir):
        print(f"✓ Directory created successfully: {dto_dir}")
    else:
        print(f"✗ Failed to create directory")
        return False
    
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
}
"""
    
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
}
"""
    
    # Create SkillNodeDto.java
    print("\nCreating SkillNodeDto.java...")
    skillnodedto_path = os.path.join(dto_dir, 'SkillNodeDto.java')
    with open(skillnodedto_path, 'w') as f:
        f.write(skillnodedto_content)
    
    if os.path.exists(skillnodedto_path):
        print(f"✓ SkillNodeDto.java created successfully")
    
    # Create PortfolioDNADto.java
    print("\nCreating PortfolioDNADto.java...")
    portfoliodnadto_path = os.path.join(dto_dir, 'PortfolioDNADto.java')
    with open(portfoliodnadto_path, 'w') as f:
        f.write(portfoliodnadto_content)
    
    if os.path.exists(portfoliodnadto_path):
        print(f"✓ PortfolioDNADto.java created successfully")
    
    # Verification
    print("\n" + "=" * 60)
    print("VERIFICATION")
    print("=" * 60)
    
    if os.path.exists(skillnodedto_path):
        print("✓ SkillNodeDto.java exists")
    if os.path.exists(portfoliodnadto_path):
        print("✓ PortfolioDNADto.java exists")
    
    # Display file contents
    print("\n" + "=" * 60)
    print("SkillNodeDto.java CONTENT")
    print("=" * 60)
    print(skillnodedto_content)
    
    print("\n" + "=" * 60)
    print("PortfolioDNADto.java CONTENT")
    print("=" * 60)
    print(portfoliodnadto_content)
    
    print("\n" + "=" * 60)
    print("TASK COMPLETED")
    print("=" * 60)
    print("All DTO files have been created and verified.")
    print(f"Location: {dto_dir}")
    
    return True

if __name__ == '__main__':
    success = create_dto_files()
    sys.exit(0 if success else 1)
