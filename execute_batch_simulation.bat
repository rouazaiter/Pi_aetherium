@echo off
REM Batch file execution simulator - manually creates files and outputs

echo Creating dto directory...
if not exist "src\main\java\jihen\portfolio\dto" (
    mkdir "src\main\java\jihen\portfolio\dto"
)

REM Check if directory was created
if exist "src\main\java\jihen\portfolio\dto" (
    echo Directory created successfully: src\main\java\jihen\portfolio\dto
) else (
    echo Failed to create directory
    exit /b 1
)

echo.
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

REM Verify file creation
if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo SkillNodeDto.java created successfully at src\main\java\jihen\portfolio\dto\SkillNodeDto.java
)

echo.
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

REM Verify file creation
if exist "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java" (
    echo PortfolioDNADto.java created successfully at src\main\java\jihen\portfolio\dto\PortfolioDNADto.java
)

echo.
echo.
echo ====== VERIFICATION ======
echo.

if exist "src\main\java\jihen\portfolio\dto\SkillNodeDto.java" (
    echo [OK] SkillNodeDto.java exists
)
if exist "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java" (
    echo [OK] PortfolioDNADto.java exists
)

echo.
echo.
echo ====== SkillNodeDto.java CONTENT ======
echo.
type "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo.
echo.
echo ====== PortfolioDNADto.java CONTENT ======
echo.
type "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo.
echo ====== TASK COMPLETED ======
echo All DTO files have been created and verified.
