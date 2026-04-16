#!/usr/bin/env python3
"""
Direct inline DTO creator - no dependencies
Executes the exact logic needed to create DTO files
"""

import os
import sys

# Change to working directory
os.chdir(r"C:\Users\jihen\Downloads\portfolio")

# Define the DTO directory path (singular 'dto', not 'dtos')
dto_base = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
dto_dir = os.path.join(dto_base, "dto")

# Step 1: Create directory
print(f"Creating directory: {dto_dir}")
try:
    os.makedirs(dto_dir, exist_ok=True)
    print(f"✓ Directory created: {dto_dir}")
except Exception as e:
    print(f"✗ ERROR creating directory: {e}")
    sys.exit(1)

# Step 2: Create SkillNodeDto.java
skill_content = '''package jihen.portfolio.dto;

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

skill_path = os.path.join(dto_dir, "SkillNodeDto.java")
try:
    with open(skill_path, 'w', encoding='utf-8') as f:
        f.write(skill_content)
    print(f"✓ File created: {skill_path}")
except Exception as e:
    print(f"✗ ERROR creating SkillNodeDto.java: {e}")
    sys.exit(1)

# Step 3: Create PortfolioDNADto.java
dna_content = '''package jihen.portfolio.dto;

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

dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
try:
    with open(dna_path, 'w', encoding='utf-8') as f:
        f.write(dna_content)
    print(f"✓ File created: {dna_path}")
except Exception as e:
    print(f"✗ ERROR creating PortfolioDNADto.java: {e}")
    sys.exit(1)

# Step 4: Verification
print("\n" + "="*70)
print("VERIFICATION")
print("="*70)

# Check directory
if not os.path.isdir(dto_dir):
    print(f"✗ ERROR: Directory does not exist!")
    sys.exit(1)
print(f"✓ Directory exists: {dto_dir}")

# Check files
if not os.path.isfile(skill_path):
    print(f"✗ ERROR: SkillNodeDto.java does not exist!")
    sys.exit(1)
print(f"✓ SkillNodeDto.java exists")

if not os.path.isfile(dna_path):
    print(f"✗ ERROR: PortfolioDNADto.java does not exist!")
    sys.exit(1)
print(f"✓ PortfolioDNADto.java exists")

# List directory contents
files = os.listdir(dto_dir)
print(f"\n✓ Files in directory: {len(files)}")
for f in sorted(files):
    fpath = os.path.join(dto_dir, f)
    size = os.path.getsize(fpath)
    print(f"  - {f} ({size} bytes)")

# Step 5: Display file contents
print("\n" + "="*70)
print("SkillNodeDto.java Content")
print("="*70)
try:
    with open(skill_path, 'r', encoding='utf-8') as f:
        print(f.read())
except Exception as e:
    print(f"ERROR reading file: {e}")
    sys.exit(1)

print("\n" + "="*70)
print("PortfolioDNADto.java Content")
print("="*70)
try:
    with open(dna_path, 'r', encoding='utf-8') as f:
        print(f.read())
except Exception as e:
    print(f"ERROR reading file: {e}")
    sys.exit(1)

print("\n" + "="*70)
print("✓ TASK COMPLETED SUCCESSFULLY!")
print("="*70)

sys.exit(0)
