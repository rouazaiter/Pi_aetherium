# DTO Creation Setup Instructions

This document explains how to create the DTO files in the correct location.

## Quick Start - Choose Your Method

### Method 1: Python (Recommended)
```bash
python C:\Users\jihen\Downloads\portfolio\final_create_dto.py
```

Or:
```bash
python C:\Users\jihen\Downloads\portfolio\mkdir_dto.py
python C:\Users\jihen\Downloads\portfolio\setup_new_dto_files.py
```

### Method 2: PowerShell
```powershell
powershell -ExecutionPolicy Bypass -File "C:\Users\jihen\Downloads\portfolio\create_dto_setup.ps1"
```

### Method 3: Batch File
```cmd
C:\Users\jihen\Downloads\portfolio\create_dto_setup.bat
```

### Method 4: Java Program
```bash
cd C:\Users\jihen\Downloads\portfolio
javac CreateDtoNow.java
java CreateDtoNow
```

Or use the existing:
```bash
cd C:\Users\jihen\Downloads\portfolio
javac SetupDTO.java
java SetupDTO
```

## What Gets Created

All methods above will create:

**Directory:** `C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto`

**Files:**
1. `SkillNodeDto.java` - DTO with fields: id, name, category, isTrendy, description, searchCount
2. `PortfolioDNADto.java` - DTO with fields: portfolioId, title, bio, totalViews, followerCount, isVerified, skills, projectCategories, primaryFocus, expertise

Both files include all necessary Lombok annotations (@Getter, @Setter, @AllArgsConstructor, @NoArgsConstructor, @Builder).

## Verification

After running any of the above methods, verify by listing the directory:
```bash
dir C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto
```

Or in PowerShell:
```powershell
Get-ChildItem -Path "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
```

You should see:
- SkillNodeDto.java
- PortfolioDNADto.java
