#!/usr/bin/env python3
import os

# Create directory
dir_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
os.makedirs(dir_path, exist_ok=True)

# Create SkillNodeDto.java
with open(os.path.join(dir_path, "SkillNodeDto.java"), "w") as f:
    f.write("""package jihen.portfolio.dto;

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
""")

# Create PortfolioDNADto.java
with open(os.path.join(dir_path, "PortfolioDNADto.java"), "w") as f:
    f.write("""package jihen.portfolio.dto;

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
""")

# Display results
print("\n" + "="*70)
print("TASK COMPLETED SUCCESSFULLY!")
print("="*70)
print(f"\nDirectory created: {dir_path}")
print(f"Directory exists: {os.path.isdir(dir_path)}")
print(f"\nFiles created:")
for item in os.listdir(dir_path):
    print(f"  ✓ {item}")

print("\n" + "="*70)
print("SkillNodeDto.java")
print("="*70)
with open(os.path.join(dir_path, "SkillNodeDto.java")) as f:
    print(f.read())

print("\n" + "="*70)
print("PortfolioDNADto.java")
print("="*70)
with open(os.path.join(dir_path, "PortfolioDNADto.java")) as f:
    print(f.read())
