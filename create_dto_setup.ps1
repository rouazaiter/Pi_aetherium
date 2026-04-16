param()

# Create the DTO directory
$dtoDir = "C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
if (-not (Test-Path $dtoDir)) {
    New-Item -ItemType Directory -Path $dtoDir -Force | Out-Null
    Write-Host "Created directory: $dtoDir"
} else {
    Write-Host "Directory already exists: $dtoDir"
}

# Create SkillNodeDto.java
$skillNodeContent = @'
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

$skillNodePath = Join-Path $dtoDir "SkillNodeDto.java"
Set-Content -Path $skillNodePath -Value $skillNodeContent -Encoding UTF8
Write-Host "Created: $skillNodePath"

# Create PortfolioDNADto.java
$portfolioDNAContent = @'
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

$portfolioDNAPath = Join-Path $dtoDir "PortfolioDNADto.java"
Set-Content -Path $portfolioDNAPath -Value $portfolioDNAContent -Encoding UTF8
Write-Host "Created: $portfolioDNAPath"

# Verify
Write-Host "`nDirectory contents:"
Get-ChildItem -Path $dtoDir | ForEach-Object { Write-Host "  - $($_.Name)" }

Write-Host "`nDTO setup completed successfully!"
