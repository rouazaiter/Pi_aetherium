@echo off
cd /d "C:\Users\jihen\Downloads\portfolio"
mkdir src\main\java\jihen\portfolio\dto 2>nul
del src\main\java\jihen\portfolio\SkillNodeDto.java 2>nul

(
echo package jihen.portfolio.dto;
echo.
echo import lombok.*;
echo.
echo @Getter
echo @Setter
echo @AllArgsConstructor
echo @NoArgsConstructor
echo @Builder
echo public class SkillNodeDto {
echo     private Long id;
echo     private String name;
echo     private String category;
echo     private Boolean isTrendy;
echo     private String description;
echo     private Integer searchCount;
echo }
) > src\main\java\jihen\portfolio\dto\SkillNodeDto.java

(
echo package jihen.portfolio.dto;
echo.
echo import lombok.*;
echo import java.util.Set;
echo.
echo @Getter
echo @Setter
echo @AllArgsConstructor
echo @NoArgsConstructor
echo @Builder
echo public class PortfolioDNADto {
echo     private Long portfolioId;
echo     private String title;
echo     private String bio;
echo     private Integer totalViews;
echo     private Integer followerCount;
echo     private Boolean isVerified;
echo     private Set^<SkillNodeDto^> skills;
echo     private Set^<String^> projectCategories;
echo     private String primaryFocus;
echo     private String expertise;
echo }
) > src\main\java\jihen\portfolio\dto\PortfolioDNADto.java

echo Files created successfully!
dir src\main\java\jihen\portfolio\dto\
