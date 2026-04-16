import os
import pathlib

# Create the dto directory
dto_dir = pathlib.Path(r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto")
dto_dir.mkdir(parents=True, exist_ok=True)

# Create SkillNodeDto.java
skill_node_content = '''package jihen.portfolio.dto;

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

skill_file = dto_dir / "SkillNodeDto.java"
skill_file.write_text(skill_node_content)
print(f"Created: {skill_file}")

# Create PortfolioDNADto.java
portfolio_dna_content = '''package jihen.portfolio.dto;

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

portfolio_file = dto_dir / "PortfolioDNADto.java"
portfolio_file.write_text(portfolio_dna_content)
print(f"Created: {portfolio_file}")

# Verify
print(f"\nDirectory exists: {dto_dir.exists()}")
print(f"Files in directory:")
for f in dto_dir.iterdir():
    print(f"  - {f.name}")
