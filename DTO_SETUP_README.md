# DTO Setup - Quick Start Guide

## Overview
This repository contains several automated scripts to create the DTO (Data Transfer Object) directory and files for your Spring Boot portfolio project.

**Target Location:**
```
C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto\
```

**Files to be Created:**
1. `SkillNodeDto.java` - Lombok-annotated DTO with skill-related fields
2. `PortfolioDNADto.java` - Lombok-annotated DTO with portfolio metadata

## Recommended Methods (in order)

### 1. ⭐ Python (BEST OPTION - Most Reliable)
```bash
# Run the master setup script
python master_dto_setup.py

# OR use any of these alternatives:
python final_create_dto.py
python setup_dto_complete.py
python C:\Users\jihen\Downloads\portfolio\setup_new_dto_files.py
```

**Why Python?** It has the best cross-platform support and doesn't require compilation.

### 2. Batch File (Windows Native)
```bash
C:\Users\jihen\Downloads\portfolio\create_dto_setup.bat
```

### 3. PowerShell (Advanced)
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\jihen\Downloads\portfolio\create_dto_setup.ps1"
```

### 4. Java Program
```bash
# Compile and run
cd C:\Users\jihen\Downloads\portfolio
javac SetupDTOFiles.java
java SetupDTOFiles

# OR using run script
C:\Users\jihen\Downloads\portfolio\run_setup_now.bat
```

## What Gets Created

### Directory Structure
```
src/main/java/jihen/portfolio/
├── dto/  ← NEW DIRECTORY CREATED
│   ├── SkillNodeDto.java
│   └── PortfolioDNADto.java
├── controllers/
├── entities/
├── repositories/
└── ... other directories
```

### SkillNodeDto.java
```java
package jihen.portfolio.dto;

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
```

### PortfolioDNADto.java
```java
package jihen.portfolio.dto;

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
```

## Verification

After running any setup script, verify the files were created:

### Command Prompt / PowerShell
```cmd
dir C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
```

### Or with Git Bash / WSL
```bash
ls -la C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
```

**Expected Output:**
```
Directory of C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto

PortfolioDNADto.java
SkillNodeDto.java
```

## Troubleshooting

### Python Error: "ModuleNotFoundError"
Your Python installation is missing required modules. Try:
```bash
python -m pip install --upgrade pip
```

### "File not found" or "Path does not exist"
Ensure you're running the script from the correct directory or use the full path:
```bash
cd C:\Users\jihen\Downloads\portfolio
python master_dto_setup.py
```

### Java Compilation Error
Ensure Java is installed and in your PATH:
```bash
java -version
javac -version
```

### Permission Denied
Run your terminal as Administrator:
1. Right-click on Command Prompt/PowerShell
2. Select "Run as administrator"
3. Navigate to the portfolio directory
4. Run the setup script

## Available Setup Scripts

| Script | Type | Command |
|--------|------|---------|
| `master_dto_setup.py` | Python | `python master_dto_setup.py` |
| `final_create_dto.py` | Python | `python final_create_dto.py` |
| `setup_dto_complete.py` | Python | `python setup_dto_complete.py` |
| `setup_new_dto_files.py` | Python | `python setup_new_dto_files.py` |
| `create_dto_setup.bat` | Batch | `create_dto_setup.bat` |
| `create_dto_setup.ps1` | PowerShell | `powershell -ExecutionPolicy Bypass -File create_dto_setup.ps1` |
| `SetupDTOFiles.java` | Java | `javac SetupDTOFiles.java && java SetupDTOFiles` |
| `SetupDTO.java` | Java | `javac SetupDTO.java && java SetupDTO` |
| `run_setup_now.bat` | Batch (wrapper) | `run_setup_now.bat` |

## FAQ

**Q: Why are there so many setup scripts?**  
A: Different environments and preferences require different approaches. Choose what works best for you.

**Q: Can I manually create the files instead?**  
A: Yes! Create the directory `src\main\java\jihen\portfolio\dto\` and manually create the two Java files with the content shown above.

**Q: Do I need all these scripts?**  
A: No, you only need to run ONE script. Pick your preferred method from the list above.

**Q: Will this affect my existing code?**  
A: No! This only creates new files in the `dto/` directory. It doesn't modify any existing code.

## Next Steps

After the DTO files are created:

1. **Rebuild your project** to ensure Maven/IDE recognizes the new files
2. **Update your Lombok dependency** in `pom.xml` if not already present
3. **Create repository interfaces** for these DTOs if needed
4. **Add service methods** to use these DTOs

## Support

If you encounter issues:
1. Ensure you have write permissions to the portfolio directory
2. Close any IDE or file explorer that might lock files
3. Try running the script as Administrator
4. Check that your Python/Java installation is correct
