import os
import pathlib

dto_path = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
pathlib.Path(dto_path).mkdir(parents=True, exist_ok=True)

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

with open(os.path.join(dto_path, 'SkillNodeDto.java'), 'w') as f:
    f.write(skill_content)
with open(os.path.join(dto_path, 'PortfolioDNADto.java'), 'w') as f:
    f.write(dna_content)

print("✓ Created directory: " + dto_path)
print("✓ Created SkillNodeDto.java")
print("✓ Created PortfolioDNADto.java")
print("\n✓ Files created successfully!")
print("Directory: " + dto_path)
print("Files:")
for f in sorted(os.listdir(dto_path)):
    print("  - " + f)
