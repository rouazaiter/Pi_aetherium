import os
import shutil
import sys

# Full path to the DTO directory (singular - dto, not dtos)
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"

try:
    # Remove existing directory if it exists
    if os.path.exists(dto_dir):
        shutil.rmtree(dto_dir)
        print(f"Removed existing directory: {dto_dir}")
    
    # Create the directory
    os.makedirs(dto_dir, exist_ok=True)
    print(f"✓ Directory created: {dto_dir}\n")
    
    # SkillNodeDto.java
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
    print(f"✓ Created: {skill_path}")
    
    # PortfolioDNADto.java
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
    print(f"✓ Created: {dna_path}\n")
    
    # Verify
    print("=" * 70)
    print("VERIFICATION")
    print("=" * 70)
    print(f"Directory exists: {os.path.isdir(dto_dir)}")
    print(f"SkillNodeDto.java exists: {os.path.isfile(skill_path)}")
    print(f"PortfolioDNADto.java exists: {os.path.isfile(dna_path)}\n")
    
    print("Directory contents:")
    for f in os.listdir(dto_dir):
        fpath = os.path.join(dto_dir, f)
        size = os.path.getsize(fpath)
        print(f"  {f} ({size} bytes)")
    
    # Display contents
    print("\n" + "=" * 70)
    print("FILE CONTENTS: SkillNodeDto.java")
    print("=" * 70)
    with open(skill_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n" + "=" * 70)
    print("FILE CONTENTS: PortfolioDNADto.java")
    print("=" * 70)
    with open(dna_path, 'r', encoding='utf-8') as f:
        print(f.read())
    
    print("\n✓✓✓ SUCCESS - All files created and verified! ✓✓✓")

except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
