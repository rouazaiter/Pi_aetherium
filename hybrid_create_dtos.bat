@echo off
REM Hybrid batch/Python script to create DTO directory and files

cd /d "C:\Users\jihen\Downloads\portfolio"

REM Try to create directory using mkdir (should work on Windows)
mkdir "src\main\java\jihen\portfolio\dto" 2>nul

REM Fallback: Use Python via batch (if Python is available)
python -c "import os; os.makedirs(r'C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto', exist_ok=True); print('DTO directory created')" 2>nul

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
) > "src\main\java\jihen\portfolio\dto\SkillNodeDto.java"

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
) > "src\main\java\jihen\portfolio\dto\PortfolioDNADto.java"

REM Verify files
echo.
echo Listing dto directory:
dir "src\main\java\jihen\portfolio\dto\" 2>nul || echo (Directory may not exist yet)

REM Show file contents via Python if the directory didn't get created
python << 'EOF'
import os
dto_dir = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
if os.path.isdir(dto_dir):
    print("DTO directory exists!")
    print("Files:")
    for f in os.listdir(dto_dir):
        print(f"  - {f}")
else:
    print("NOTE: DTO directory not yet created via mkdir, but files may exist")
EOF

echo.
echo Process complete!
