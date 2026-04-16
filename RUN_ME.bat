@echo off
REM Create DTO Directory and Files - Run this batch file!
REM Location: C:\Users\jihen\Downloads\portfolio\RUN_ME.bat

setlocal enabledelayedexpansion

cd /d C:\Users\jihen\Downloads\portfolio

echo Creating directory...
mkdir "src\main\java\jihen\portfolio\dto" 2>nul

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
echo ============================================================
echo VERIFICATION
echo ============================================================
echo.
echo Directory listing:
dir "src\main\java\jihen\portfolio\dto"

echo.
echo SkillNodeDto.java contents:
echo ============================================================
type "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

echo.
echo.
echo PortfolioDNADto.java contents:
echo ============================================================
type "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

echo.
echo ✓ TASK COMPLETED SUCCESSFULLY!
echo.
pause
