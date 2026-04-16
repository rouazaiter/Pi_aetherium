import os, pathlib
d=r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto'
pathlib.Path(d).mkdir(parents=True,exist_ok=True)
s='package jihen.portfolio.dto;\n\nimport lombok.*;\n\n@Getter\n@Setter\n@AllArgsConstructor\n@NoArgsConstructor\n@Builder\npublic class SkillNodeDto {\n    private Long id;\n    private String name;\n    private String category;\n    private Boolean isTrendy;\n    private String description;\n    private Integer searchCount;\n}\n'
p='package jihen.portfolio.dto;\n\nimport lombok.*;\nimport java.util.Set;\n\n@Getter\n@Setter\n@AllArgsConstructor\n@NoArgsConstructor\n@Builder\npublic class PortfolioDNADto {\n    private Long portfolioId;\n    private String title;\n    private String bio;\n    private Integer totalViews;\n    private Integer followerCount;\n    private Boolean isVerified;\n    private Set<SkillNodeDto> skills;\n    private Set<String> projectCategories;\n    private String primaryFocus;\n    private String expertise;\n}\n'
open(os.path.join(d,'SkillNodeDto.java'),'w').write(s)
open(os.path.join(d,'PortfolioDNADto.java'),'w').write(p)
print('✓ Created directory: '+d)
print('✓ Created SkillNodeDto.java')
print('✓ Created PortfolioDNADto.java')
print('\n✓ Files created successfully!')
print('Directory: '+d)
print('Files:')
for f in sorted(os.listdir(d)):
    fp=os.path.join(d,f)
    if os.path.isfile(fp):
        print(f'  - {f} ({os.path.getsize(fp)} bytes)')
print('\n--- SkillNodeDto.java ---')
print(open(os.path.join(d,'SkillNodeDto.java')).read())
print('\n--- PortfolioDNADto.java ---')
print(open(os.path.join(d,'PortfolioDNADto.java')).read())
