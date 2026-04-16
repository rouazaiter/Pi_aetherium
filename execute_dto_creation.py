import os
import sys

# Create directory structure
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"

try:
    # Create all parent directories
    os.makedirs(dto_dir, exist_ok=True)
    print(f"✓ Directory created: {dto_dir}")
    
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
    print(f"✓ Created: SkillNodeDto.java ({os.path.getsize(skill_path)} bytes)")
    
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
    print(f"✓ Created: PortfolioDNADto.java ({os.path.getsize(dna_path)} bytes)")
    
    # Verification section
    print("\n" + "=" * 70)
    print("VERIFICATION")
    print("=" * 70)
    print(f"Directory exists: {os.path.isdir(dto_dir)}")
    print(f"SkillNodeDto.java exists: {os.path.isfile(skill_path)}")
    print(f"PortfolioDNADto.java exists: {os.path.isfile(dna_path)}")
    
    print("\nDirectory contents:")
    for filename in sorted(os.listdir(dto_dir)):
        fpath = os.path.join(dto_dir, filename)
        size = os.path.getsize(fpath)
        print(f"  ✓ {filename} ({size} bytes)")
    
    # Display complete file contents
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
    
    print("\n" + "=" * 70)
    print("✓✓✓ SUCCESS - All files created and verified! ✓✓✓")
    print("=" * 70)

except Exception as e:
    print(f"ERROR: {e}")
    import traceback
    traceback.print_exc()
    sys.exit(1)
