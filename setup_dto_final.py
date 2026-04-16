import os

# Create the dto directory structure
base_path = r'C:\Users\jihen\Downloads\portfolio'
dto_path = os.path.join(base_path, 'src', 'main', 'java', 'jihen', 'portfolio', 'dto')
os.makedirs(dto_path, exist_ok=True)

# SkillNodeDto.java content
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

# PortfolioDNADto.java content
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

# Write the files
skill_node_file = os.path.join(dto_path, 'SkillNodeDto.java')
portfolio_dna_file = os.path.join(dto_path, 'PortfolioDNADto.java')

with open(skill_node_file, 'w', encoding='utf-8') as f:
    f.write(skill_node_content)

with open(portfolio_dna_file, 'w', encoding='utf-8') as f:
    f.write(portfolio_dna_content)

print(f"✓ Directory created: {dto_path}")
print(f"✓ SkillNodeDto.java created")
print(f"✓ PortfolioDNADto.java created")
print(f"\n=== VERIFICATION ===")
print(f"Files in {dto_path}:")
for file in os.listdir(dto_path):
    if os.path.isfile(os.path.join(dto_path, file)):
        print(f"  ✓ {file}")
