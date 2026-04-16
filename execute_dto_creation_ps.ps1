$ErrorActionPreference = "Stop"

# Create directory
$dir = "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
New-Item -ItemType Directory -Path $dir -Force | Out-Null
Write-Host "✓ Directory created: $dir"

# SkillNodeDto content
$skillDto = @'
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
'@

# PortfolioDNADto content
$portfolioDto = @'
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
'@

# Create files
$skillPath = "$dir\SkillNodeDto.java"
$portfolioPath = "$dir\PortfolioDNADto.java"

Set-Content -Path $skillPath -Value $skillDto -Encoding UTF8
Write-Host "✓ File created: $skillPath"

Set-Content -Path $portfolioPath -Value $portfolioDto -Encoding UTF8
Write-Host "✓ File created: $portfolioPath"

# Verification
Write-Host ""
Write-Host ("=" * 70)
Write-Host "VERIFICATION"
Write-Host ("=" * 70)

$dirInfo = Get-Item $dir
Write-Host "✓ Directory exists: $dir"
Write-Host "✓ Directory is absolute: $(Test-Path -IsValid $dir)"

$files = Get-ChildItem -Path $dir -File
Write-Host "✓ Files in directory: $($files.Count)"
foreach ($file in $files) {
    Write-Host "  - $($file.Name) ($($file.Length) bytes)"
}

# Display contents
Write-Host ""
Write-Host ("=" * 70)
Write-Host "SkillNodeDto.java Content"
Write-Host ("=" * 70)
Get-Content -Path $skillPath

Write-Host ""
Write-Host ("=" * 70)
Write-Host "PortfolioDNADto.java Content"
Write-Host ("=" * 70)
Get-Content -Path $portfolioPath

Write-Host ""
Write-Host ("=" * 70)
Write-Host "✓ TASK COMPLETED SUCCESSFULLY!"
Write-Host ("=" * 70)
