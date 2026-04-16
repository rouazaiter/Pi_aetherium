import os

# Create the directory
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_dir, exist_ok=True)

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

with open(os.path.join(dto_dir, "SkillNodeDto.java"), 'w', encoding='utf-8') as f:
    f.write(skill_content)

# Create PortfolioDNADto.java
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

with open(os.path.join(dto_dir, "PortfolioDNADto.java"), 'w', encoding='utf-8') as f:
    f.write(dna_content)

# Print output
print("=" * 70)
print("DIRECTORY CREATION CONFIRMATION")
print("=" * 70)
print(f"✓ Directory created: {dto_dir}\n")

print("✓ Created: " + os.path.join(dto_dir, "SkillNodeDto.java"))
print("✓ Created: " + os.path.join(dto_dir, "PortfolioDNADto.java") + "\n")

print("=" * 70)
print("VERIFICATION")
print("=" * 70)
print(f"Directory exists: {os.path.isdir(dto_dir)}")
print(f"SkillNodeDto.java exists: {os.path.isfile(os.path.join(dto_dir, 'SkillNodeDto.java'))}")
print(f"PortfolioDNADto.java exists: {os.path.isfile(os.path.join(dto_dir, 'PortfolioDNADto.java'))}\n")

print("Directory contents:")
for f in os.listdir(dto_dir):
    fpath = os.path.join(dto_dir, f)
    size = os.path.getsize(fpath)
    print(f"  {f} ({size} bytes)")

# Display contents
print("\n" + "=" * 70)
print("FILE CONTENTS: SkillNodeDto.java")
print("=" * 70)
with open(os.path.join(dto_dir, "SkillNodeDto.java"), 'r', encoding='utf-8') as f:
    print(f.read())

print("\n" + "=" * 70)
print("FILE CONTENTS: PortfolioDNADto.java")
print("=" * 70)
with open(os.path.join(dto_dir, "PortfolioDNADto.java"), 'r', encoding='utf-8') as f:
    print(f.read())

print("\n✓✓✓ SUCCESS - All files created and verified! ✓✓✓")
