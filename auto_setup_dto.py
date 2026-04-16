#!/usr/bin/env python3
"""
Auto-executing DTO setup when imported
"""
import os

def setup_dtos():
    """Create DTO directory and files"""
    dir_path = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto"
    
    try:
        os.makedirs(dir_path, exist_ok=True)
        
        # SkillNodeDto.java
        with open(os.path.join(dir_path, "SkillNodeDto.java"), "w") as f:
            f.write('''package jihen.portfolio.dto;

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
''')
        
        # PortfolioDNADto.java
        with open(os.path.join(dir_path, "PortfolioDNADto.java"), "w") as f:
            f.write('''package jihen.portfolio.dto;

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
''')
        
        return True
    except Exception as e:
        print(f"Error: {e}")
        return False

# Auto-execute on import or run
if __name__ == "__main__":
    if setup_dtos():
        print("✓ DTO setup completed successfully!")
    else:
        print("✗ DTO setup failed!")
else:
    # Execute when imported
    setup_dtos()
