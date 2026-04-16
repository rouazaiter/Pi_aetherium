import os
import pathlib

# Create the directory
dir_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
pathlib.Path(dir_path).mkdir(parents=True, exist_ok=True)
print(f"Directory created: {dir_path}")

# Create SkillNodeDto.java
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

skill_node_path = os.path.join(dir_path, "SkillNodeDto.java")
with open(skill_node_path, 'w', encoding='utf-8') as f:
    f.write(skill_node_content)
print(f"File created: {skill_node_path}")

# Create PortfolioDNADto.java
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

portfolio_dna_path = os.path.join(dir_path, "PortfolioDNADto.java")
with open(portfolio_dna_path, 'w', encoding='utf-8') as f:
    f.write(portfolio_dna_content)
print(f"File created: {portfolio_dna_path}")

# Verify and display
print("\n=== VERIFICATION ===")
print(f"Directory exists: {os.path.exists(dir_path)}")
print(f"\nFiles in directory:")
for file in os.listdir(dir_path):
    print(f"  - {file}")

print("\n=== FILE: SkillNodeDto.java ===")
with open(skill_node_path, 'r', encoding='utf-8') as f:
    print(f.read())

print("\n=== FILE: PortfolioDNADto.java ===")
with open(portfolio_dna_path, 'r', encoding='utf-8') as f:
    print(f.read())
