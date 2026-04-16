#!/usr/bin/env python3
import os
import sys

# Create the directory
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_dir, exist_ok=True)

# SkillNodeDto content
skill_node_content = """package jihen.portfolio.dto;

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
portfolio_dna_content = """package jihen.portfolio.dto;

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
skill_node_path = os.path.join(dto_dir, "SkillNodeDto.java")
with open(skill_node_path, 'w', encoding='utf-8') as f:
    f.write(skill_node_content)

# Create PortfolioDNADto.java
portfolio_dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
with open(portfolio_dna_path, 'w', encoding='utf-8') as f:
    f.write(portfolio_dna_content)

# Print output like the original script
print("="*70)
print("✓ Directory created: " + dto_dir)
print("✓ File created: " + skill_node_path)
print("✓ File created: " + portfolio_dna_path)
print()
print("="*70)
print("VERIFICATION")
print("="*70)

if not os.path.isdir(dto_dir):
    print("✗ ERROR: Directory does not exist!")
    sys.exit(1)

print(f"✓ Directory exists: {dto_dir}")

files = os.listdir(dto_dir)
if len(files) != 2:
    print(f"✗ ERROR: Expected 2 files, found {len(files)}")
    sys.exit(1)

print(f"✓ Files in directory: {len(files)}")
for f in sorted(files):
    fpath = os.path.join(dto_dir, f)
    size = os.path.getsize(fpath)
    print(f"  - {f} ({size} bytes)")

# Display contents
print()
print("="*70)
print("SkillNodeDto.java Content")
print("="*70)
with open(skill_node_path, 'r', encoding='utf-8') as f:
    print(f.read())

print("="*70)
print("PortfolioDNADto.java Content")
print("="*70)
with open(portfolio_dna_path, 'r', encoding='utf-8') as f:
    print(f.read())

print("="*70)
print("✓ TASK COMPLETED SUCCESSFULLY!")
print("="*70)
