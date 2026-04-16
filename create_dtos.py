#!/usr/bin/env python3
import os
from pathlib import Path

# Create directory
dto_dir = Path(r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto')
dto_dir.mkdir(parents=True, exist_ok=True)
print(f'Created directory: {dto_dir}')

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
skill_node_file = dto_dir / 'SkillNodeDto.java'
skill_node_file.write_text(skill_node_content)
print(f'Created: {skill_node_file}')

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
portfolio_dna_file = dto_dir / 'PortfolioDNADto.java'
portfolio_dna_file.write_text(portfolio_dna_content)
print(f'Created: {portfolio_dna_file}')

print('All DTOs created successfully!')
print(f'\nVerifying files exist:')
print(f'SkillNodeDto.java exists: {skill_node_file.exists()}')
print(f'PortfolioDNADto.java exists: {portfolio_dna_file.exists()}')
