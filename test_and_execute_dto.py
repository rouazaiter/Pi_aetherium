#!/usr/bin/env python3
import sys
import os

print("Python version:", sys.version)
print("Python executable:", sys.executable)
print("Current directory:", os.getcwd())
print("Directory listing:")
for item in os.listdir("."):
    print(f"  {item}")

# Now run the DTO creation
base_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
dto_dir = os.path.join(base_dir, "dto")

print(f"\nTarget DTO directory: {dto_dir}")
print(f"Parent directory exists: {os.path.isdir(base_dir)}")

# Create the directory
try:
    os.makedirs(dto_dir, exist_ok=True)
    print(f"✓ Successfully created/verified directory: {dto_dir}")
    
    # Create SkillNodeDto.java
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
    with open(skill_path, 'w', encoding='utf-8') as f:
        f.write(skill_content)
    print(f"✓ Created: SkillNodeDto.java")
    
    # Create PortfolioDNADto.java
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
    with open(dna_path, 'w', encoding='utf-8') as f:
        f.write(dna_content)
    print(f"✓ Created: PortfolioDNADto.java")
    
    # Verification
    print("\n" + "="*70)
    print("VERIFICATION")
    print("="*70)
    
    print(f"✓ Directory exists: {dto_dir}")
    print(f"✓ Directory is valid: {os.path.isdir(dto_dir)}")
    
    files = os.listdir(dto_dir)
    print(f"✓ Number of files in directory: {len(files)}")
    
    for fname in sorted(files):
        fpath = os.path.join(dto_dir, fname)
        size = os.path.getsize(fpath)
        print(f"  - {fname} ({size} bytes)")
    
    # Display full contents
    print("\n" + "="*70)
    print("SkillNodeDto.java Content")
    print("="*70)
    with open(skill_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n" + "="*70)
    print("PortfolioDNADto.java Content")
    print("="*70)
    with open(dna_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n" + "="*70)
    print("✓✓✓ TASK COMPLETED SUCCESSFULLY! ✓✓✓")
    print("="*70)
    
except Exception as e:
    print(f"✗ ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
