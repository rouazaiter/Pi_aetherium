#!/usr/bin/env python3
import os
import sys

# Create the directory
dir_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dir_path, exist_ok=True)

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

# Write files
skill_node_path = os.path.join(dir_path, "SkillNodeDto.java")
portfolio_dna_path = os.path.join(dir_path, "PortfolioDNADto.java")

try:
    with open(skill_node_path, 'w') as f:
        f.write(skill_node_content)
    
    with open(portfolio_dna_path, 'w') as f:
        f.write(portfolio_dna_content)
    
    print(f"Created directory: {dir_path}")
    print(f"Created file: {skill_node_path}")
    print(f"Created file: {portfolio_dna_path}")
    
    # Verify
    if os.path.exists(skill_node_path) and os.path.exists(portfolio_dna_path):
        print("\nVerification:")
        print(f"SkillNodeDto.java exists: {os.path.exists(skill_node_path)}")
        print(f"PortfolioDNADto.java exists: {os.path.exists(portfolio_dna_path)}")
        print("\nSuccess! Both files created.")
    else:
        print("Error: Files were not created properly", file=sys.stderr)
        sys.exit(1)
        
except Exception as e:
    print(f"Error: {e}", file=sys.stderr)
    sys.exit(1)
