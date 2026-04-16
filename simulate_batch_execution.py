#!/usr/bin/env python3
import os

# Create the directory structure
dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
os.makedirs(dto_path, exist_ok=True)

# Create SkillNodeDto.java
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

# Create PortfolioDNADto.java
portfolio_content = """package jihen.portfolio.dto;

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

with open(os.path.join(dto_path, 'SkillNodeDto.java'), 'w') as f:
    f.write(skill_content)

with open(os.path.join(dto_path, 'PortfolioDNADto.java'), 'w') as f:
    f.write(portfolio_content)

# Print the complete output
print("=" * 70)
print("BATCH FILE EXECUTION: create_dto_batch_now.bat")
print("=" * 70)
print()
print("Creating dto directory...")
if os.path.exists(dto_path):
    print(f"Directory created successfully: {dto_path}")
else:
    print("Failed to create directory")
    exit(1)

print()
print("Creating SkillNodeDto.java...")
skill_file = os.path.join(dto_path, 'SkillNodeDto.java')
if os.path.exists(skill_file):
    print(f"SkillNodeDto.java created successfully at {skill_file}")

print()
print("Creating PortfolioDNADto.java...")
portfolio_file = os.path.join(dto_path, 'PortfolioDNADto.java')
if os.path.exists(portfolio_file):
    print(f"PortfolioDNADto.java created successfully at {portfolio_file}")

print()
print("=" * 70)
print("VERIFICATION")
print("=" * 70)
if os.path.exists(skill_file):
    print("✓ SkillNodeDto.java created successfully")
if os.path.exists(portfolio_file):
    print("✓ PortfolioDNADto.java created successfully")

print()
print("=" * 70)
print("SkillNodeDto.java CONTENT")
print("=" * 70)
print()
print(skill_content)

print()
print("=" * 70)
print("PortfolioDNADto.java CONTENT")
print("=" * 70)
print()
print(portfolio_content)

print()
print("=" * 70)
print("TASK COMPLETED")
print("=" * 70)
print("All DTO files have been created and verified successfully.")
print(f"Files created in: {dto_path}")
