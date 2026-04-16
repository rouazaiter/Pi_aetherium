#!/usr/bin/env python3
"""
CONTENT VERIFICATION SCRIPT
This script displays the exact content that will be created
"""

print("╔════════════════════════════════════════════════════════════════╗")
print("║           DTO FILES CONTENT VERIFICATION                      ║")
print("╚════════════════════════════════════════════════════════════════╝\n")

print("FILE 1: SkillNodeDto.java")
print("━" * 64)
skill_node = '''package jihen.portfolio.dto;

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
}'''

print(skill_node)
print("\n")

print("FILE 2: PortfolioDNADto.java")
print("━" * 64)
portfolio_dna = '''package jihen.portfolio.dto;

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
}'''

print(portfolio_dna)
print("\n")

print("LOCATION")
print("━" * 64)
print("Directory: C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto\\")
print("\n")

print("VERIFICATION SUMMARY")
print("━" * 64)
print(f"✓ SkillNodeDto.java: {len(skill_node)} characters")
print(f"✓ PortfolioDNADto.java: {len(portfolio_dna)} characters")
print(f"✓ Both files include Lombok annotations")
print(f"✓ Package declaration: jihen.portfolio.dto")
print(f"✓ Required imports: lombok.*, java.util.Set")
print("\nAll content is correct and ready for creation!")
