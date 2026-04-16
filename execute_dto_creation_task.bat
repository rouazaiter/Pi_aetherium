@echo off
setlocal enabledelayedexpansion

REM Create directory structure
mkdir "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" 2>nul

REM Print creation confirmation
echo.
echo ============================================================
echo DIRECTORY CREATION
echo ============================================================
echo Directory created: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto

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

echo File created: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\SkillNodeDto.java

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

echo File created: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\PortfolioDNADto.java

REM Verification section
echo.
echo ============================================================
echo VERIFICATION
echo ============================================================

REM Check if directory exists
if exist "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto" (
    echo Directory exists: C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
) else (
    echo ERROR: Directory does not exist!
    exit /b 1
)

REM List files
echo Files in directory:
for %%F in ("C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\*") do (
    echo   - %%~nxF
)

REM Show SkillNodeDto.java content
echo.
echo ============================================================
echo SkillNodeDto.java Content
echo ============================================================
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

REM Show PortfolioDNADto.java content
echo.
echo ============================================================
echo PortfolioDNADto.java Content
echo ============================================================
type "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo ============================================================
echo TASK COMPLETED SUCCESSFULLY!
echo ============================================================

endlocal
