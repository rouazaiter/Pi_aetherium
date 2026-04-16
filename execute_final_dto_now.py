import os
import shutil

# Define paths
base_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
dto_dir = os.path.join(base_dir, "dto")
dtos_dir = os.path.join(base_dir, "dtos")

# Create the dto directory
try:
    os.makedirs(dto_dir, exist_ok=True)
    print(f"✓ Directory created: {dto_dir}")
    
    # DTO file 1 content
    skill_node_dto = '''package jihen.portfolio.dto;

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
    
    # DTO file 2 content
    portfolio_dna_dto = '''package jihen.portfolio.dto;

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
    
    # Create SkillNodeDto.java
    skill_node_path = os.path.join(dto_dir, "SkillNodeDto.java")
    with open(skill_node_path, 'w', encoding='utf-8') as f:
        f.write(skill_node_dto)
    print(f"✓ File created: {skill_node_path}")
    
    # Create PortfolioDNADto.java
    portfolio_dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
    with open(portfolio_dna_path, 'w', encoding='utf-8') as f:
        f.write(portfolio_dna_dto)
    print(f"✓ File created: {portfolio_dna_path}")
    
    # Verification
    print("\n" + "="*70)
    print("VERIFICATION")
    print("="*70)
    
    if not os.path.isdir(dto_dir):
        print(f"✗ ERROR: Directory does not exist!")
        exit(1)
        
    print(f"✓ Directory exists: {dto_dir}")
    
    files = os.listdir(dto_dir)
    if len(files) != 2:
        print(f"✗ ERROR: Expected 2 files, found {len(files)}")
        exit(1)
        
    print(f"✓ Files in directory: {len(files)}")
    for f in files:
        fpath = os.path.join(dto_dir, f)
        size = os.path.getsize(fpath)
        print(f"  - {f} ({size} bytes)")
    
    # Display contents
    print("\n" + "="*70)
    print("SkillNodeDto.java Content")
    print("="*70)
    with open(skill_node_path, 'r', encoding='utf-8') as f:
        content = f.read()
        print(content)
    
    print("\n" + "="*70)
    print("PortfolioDNADto.java Content")
    print("="*70)
    with open(portfolio_dna_path, 'r', encoding='utf-8') as f:
        content = f.read()
        print(content)
    
    print("\n" + "="*70)
    print("✓ TASK COMPLETED SUCCESSFULLY!")
    print("="*70)
    
except Exception as e:
    print(f"✗ ERROR: {e}")
    import traceback
    traceback.print_exc()
    exit(1)
