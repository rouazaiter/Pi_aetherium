import os; dirs=r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'; os.makedirs(dirs, exist_ok=True); open(os.path.join(dirs, 'SkillNodeDto.java'), 'w').write('''package jihen.portfolio.dto;

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
'''); open(os.path.join(dirs, 'PortfolioDNADto.java'), 'w').write('''package jihen.portfolio.dto;

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
'''); print(f'✓ Created directory: {dirs}'); print('✓ Created SkillNodeDto.java'); print('✓ Created PortfolioDNADto.java'); [print(f'✓ File exists: {os.path.basename(f)}') for f in [os.path.join(dirs, 'SkillNodeDto.java'), os.path.join(dirs, 'PortfolioDNADto.java')] if os.path.exists(f)]; print('\n' + '='*60 + '\nSkillNodeDto.java CONTENT\n' + '='*60); print(open(os.path.join(dirs, 'SkillNodeDto.java')).read()); print('\n' + '='*60 + '\nPortfolioDNADto.java CONTENT\n' + '='*60); print(open(os.path.join(dirs, 'PortfolioDNADto.java')).read()); print('\n' + '='*60 + '\nTASK COMPLETED\n' + '='*60)
