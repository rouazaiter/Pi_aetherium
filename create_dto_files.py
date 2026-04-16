import os

# Create the dto directory
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_dir, exist_ok=True)

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

# Write the files
with open(os.path.join(dto_dir, "SkillNodeDto.java"), "w") as f:
    f.write(skill_node_content)

with open(os.path.join(dto_dir, "PortfolioDNADto.java"), "w") as f:
    f.write(portfolio_dna_content)

print(f"Created directory: {dto_dir}")
print("Created SkillNodeDto.java")
print("Created PortfolioDNADto.java")
