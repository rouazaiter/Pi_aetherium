import os

# Create directory structure
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dto_dir, exist_ok=True)
print(f"Directory created: {dto_dir}")

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

skill_node_path = os.path.join(dto_dir, "SkillNodeDto.java")
with open(skill_node_path, 'w') as f:
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

portfolio_dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
with open(portfolio_dna_path, 'w') as f:
    f.write(portfolio_dna_content)
print(f"File created: {portfolio_dna_path}")

# Verify files exist
print("\nVerification:")
print(f"Directory exists: {os.path.isdir(dto_dir)}")
print(f"SkillNodeDto.java exists: {os.path.isfile(skill_node_path)}")
print(f"PortfolioDNADto.java exists: {os.path.isfile(portfolio_dna_path)}")

# List directory contents
print(f"\nDirectory contents:")
for item in os.listdir(dto_dir):
    print(f"  - {item}")
    
# Show file contents verification
print("\n" + "="*60)
print("SkillNodeDto.java content:")
print("="*60)
with open(skill_node_path, 'r') as f:
    content = f.read()
    print(content)
    
print("\n" + "="*60)
print("PortfolioDNADto.java content:")
print("="*60)
with open(portfolio_dna_path, 'r') as f:
    content = f.read()
    print(content)
    
print("\n✓ Setup completed successfully!")
