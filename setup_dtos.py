import os
from pathlib import Path

# Create directory
dto_dir = Path(r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto')
dto_dir.mkdir(parents=True, exist_ok=True)

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

(dto_dir / 'SkillNodeDto.java').write_text(skill_content)

# Create PortfolioDNADto.java
portfolio_content = """package jihen.portfolio.dto;

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

(dto_dir / 'PortfolioDNADto.java').write_text(portfolio_content)

print("Done")
