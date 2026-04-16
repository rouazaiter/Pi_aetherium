import os

# Create the dto directory
dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
os.makedirs(dto_dir, exist_ok=True)
print(f'✓ Directory created: {dto_dir}')

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

skill_node_file = os.path.join(dto_dir, 'SkillNodeDto.java')
with open(skill_node_file, 'w', encoding='utf-8') as f:
    f.write(skill_node_content)
print('✓ Created: SkillNodeDto.java')

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

portfolio_dna_file = os.path.join(dto_dir, 'PortfolioDNADto.java')
with open(portfolio_dna_file, 'w', encoding='utf-8') as f:
    f.write(portfolio_dna_content)
print('✓ Created: PortfolioDNADto.java')

# List directory contents
print('\n=== Directory Listing ===')
files = os.listdir(dto_dir)
for f in sorted(files):
    file_path = os.path.join(dto_dir, f)
    file_size = os.path.getsize(file_path)
    print(f'{f} ({file_size} bytes)')

# Display full contents of SkillNodeDto.java
print('\n=== SkillNodeDto.java ===')
with open(skill_node_file, 'r', encoding='utf-8') as f:
    print(f.read())

# Display full contents of PortfolioDNADto.java
print('\n=== PortfolioDNADto.java ===')
with open(portfolio_dna_file, 'r', encoding='utf-8') as f:
    print(f.read())
