@echo off
REM Create the dto directory
mkdir "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" 2>nul

REM Create SkillNodeDto.java
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
) > "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

REM Create PortfolioDNADto.java
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
) > "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

REM List directory
echo.
echo Directory contents:
dir "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"

REM Show file contents
echo.
echo === SkillNodeDto.java ===
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo.
echo === PortfolioDNADto.java ===
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"
