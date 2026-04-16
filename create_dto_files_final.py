#!/usr/bin/env python3
import os
import sys

# Create the directory structure
base_path = r"C:\Users\jihen\Downloads\portfolio"
dto_path = os.path.join(base_path, "src", "main", "java", "jihen", "portfolio", "dto")

print("=" * 70)
print("DTO DIRECTORY AND FILE CREATION SCRIPT")
print("=" * 70)
print(f"\nTarget path: {dto_path}")

# Create directory
try:
    os.makedirs(dto_path, exist_ok=True)
    print(f"\n✓ Directory created successfully")
except Exception as e:
    print(f"\n✗ Error creating directory: {e}")
    sys.exit(1)

# Content for SkillNodeDto.java
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

# Content for PortfolioDNADto.java
portfoliodnadt_content = """package jihen.portfolio.dto;

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

# Write files
files_to_create = {
    "SkillNodeDto.java": skillnodedto_content,
    "PortfolioDNADto.java": portfoliodnadt_content
}

print("\n" + "-" * 70)
print("Creating files:")
print("-" * 70)

for filename, content in files_to_create.items():
    filepath = os.path.join(dto_path, filename)
    try:
        with open(filepath, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"✓ {filename} created successfully")
        print(f"  Path: {filepath}")
        print(f"  Size: {len(content)} bytes")
    except Exception as e:
        print(f"✗ Error creating {filename}: {e}")
        sys.exit(1)

# Verification
print("\n" + "-" * 70)
print("VERIFICATION")
print("-" * 70)

print(f"\n1. Directory exists: {os.path.exists(dto_path)}")
print(f"   Path: {dto_path}")

print(f"\n2. Directory contents:")
contents = os.listdir(dto_path)
for item in contents:
    full_path = os.path.join(dto_path, item)
    is_file = os.path.isfile(full_path)
    size = os.path.getsize(full_path) if is_file else "N/A"
    print(f"   - {item} (File: {is_file}, Size: {size})")

print(f"\n3. File contents verification:")
for filename in files_to_create.keys():
    filepath = os.path.join(dto_path, filename)
    if os.path.exists(filepath):
        with open(filepath, "r", encoding="utf-8") as f:
            actual_content = f.read()
        expected_content = files_to_create[filename]
        if actual_content == expected_content:
            print(f"   ✓ {filename} - Content matches exactly")
        else:
            print(f"   ✗ {filename} - Content MISMATCH")
            print(f"      Expected {len(expected_content)} bytes, got {len(actual_content)} bytes")

print("\n" + "=" * 70)
print("✓ COMPLETE - All files created and verified successfully!")
print("=" * 70)
