@echo off
setlocal enabledelayedexpansion

cd /d "C:\Users\jihen\Downloads\portfolio"

echo Creating dto directory...
mkdir "src\main\java\jihen\portfolio\dto"
if exist "src\main\java\jihen\portfolio\dto" (
    echo Directory created successfully: src\main\java\jihen\portfolio\dto
) else (
    echo Failed to create directory
    exit /b 1
)

echo Creating SkillNodeDto.java...
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
) > "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo Creating PortfolioDNADto.java...
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
) > "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo ====== VERIFICATION ======
echo.
if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo SkillNodeDto.java created successfully
)
if exist "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java" (
    echo PortfolioDNADto.java created successfully
)

echo.
echo ====== SkillNodeDto.java CONTENT ======
echo.
type "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo.
echo ====== PortfolioDNADto.java CONTENT ======
echo.
type "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo ====== TASK COMPLETED ======
