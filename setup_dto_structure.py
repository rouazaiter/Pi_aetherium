import os

# Create the directory structure
dto_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_path, exist_ok=True)

# Create SkillNodeDto.java
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

# Create PortfolioDNADto.java
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

# Write the files
with open(os.path.join(dto_path, "SkillNodeDto.java"), "w") as f:
    f.write(skillnodedto_content)

with open(os.path.join(dto_path, "PortfolioDNADto.java"), "w") as f:
    f.write(portfoliodnadt_content)

print("Files created successfully!")
print(f"\nDirectory: {dto_path}")
print(f"Contents: {os.listdir(dto_path)}")
