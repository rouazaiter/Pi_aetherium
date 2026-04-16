import os; exec('''
base = r"C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio"
dto = os.path.join(base, "dto")
os.makedirs(dto, exist_ok=True)

skill = """package jihen.portfolio.dto;
import lombok.*;
@Getter@Setter@AllArgsConstructor@NoArgsConstructor@Builder
public class SkillNodeDto {
    private Long id;
    private String name;
    private String category;
    private Boolean isTrendy;
    private String description;
    private Integer searchCount;
}"""

dna = """package jihen.portfolio.dto;
import lombok.*;
import java.util.Set;
@Getter@Setter@AllArgsConstructor@NoArgsConstructor@Builder
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
}"""

with open(os.path.join(dto, "SkillNodeDto.java"), "w") as f:
    f.write(skill)
with open(os.path.join(dto, "PortfolioDNADto.java"), "w") as f:
    f.write(dna)

print("Files created")
''')
