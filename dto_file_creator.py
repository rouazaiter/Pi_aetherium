import os
import pathlib

# Create directory structure
dto_dir = r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
pathlib.Path(dto_dir).mkdir(parents=True, exist_ok=True)
print(f'✓ Directory created: {dto_dir}')

# Create SkillNodeDto.java
skill_content = '''package jihen.portfolio.dto;

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

skill_file = os.path.join(dto_dir, 'SkillNodeDto.java')
with open(skill_file, 'w', encoding='utf-8') as f:
    f.write(skill_content)
print(f'✓ File created: SkillNodeDto.java')

# Create PortfolioDNADto.java
dna_content = '''package jihen.portfolio.dto;

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

dna_file = os.path.join(dto_dir, 'PortfolioDNADto.java')
with open(dna_file, 'w', encoding='utf-8') as f:
    f.write(dna_content)
print(f'✓ File created: PortfolioDNADto.java')

# Verify files
files = os.listdir(dto_dir)
print(f'\n✓ Files in directory ({len(files)} total):')
for f in sorted(files):
    print(f'  - {f}')

# Display contents
print('\n' + '='*70)
print('SkillNodeDto.java contents:')
print('='*70)
with open(skill_file, 'r', encoding='utf-8') as f:
    print(f.read())

print('\n' + '='*70)
print('PortfolioDNADto.java contents:')
print('='*70)
with open(dna_file, 'r', encoding='utf-8') as f:
    print(f.read())
