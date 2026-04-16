#!/usr/bin/env python3
import os
import sys
import pathlib

def main():
    try:
        # Create the directory path
        base_path = pathlib.Path(r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio\dto")
        
        # Create all parent directories
        base_path.mkdir(parents=True, exist_ok=True)
        print(f"✓ Directory created: {base_path}")
        
        # DTO file 1 content
        skill_node_dto = '''package jihen.portfolio.dto;

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
'''
        
        # DTO file 2 content
        portfolio_dna_dto = '''package jihen.portfolio.dto;

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
'''
        
        # Create SkillNodeDto.java
        skill_node_path = base_path / "SkillNodeDto.java"
        skill_node_path.write_text(skill_node_dto, encoding='utf-8')
        print(f"✓ File created: {skill_node_path}")
        
        # Create PortfolioDNADto.java
        portfolio_dna_path = base_path / "PortfolioDNADto.java"
        portfolio_dna_path.write_text(portfolio_dna_dto, encoding='utf-8')
        print(f"✓ File created: {portfolio_dna_path}")
        
        # Verification
        print("\n" + "="*70)
        print("VERIFICATION")
        print("="*70)
        
        if not base_path.is_dir():
            print(f"✗ ERROR: Directory does not exist!")
            return False
            
        print(f"✓ Directory exists: {base_path}")
        print(f"✓ Directory is absolute: {base_path.is_absolute()}")
        
        files = list(base_path.iterdir())
        print(f"✓ Files in directory: {len(files)}")
        for f in files:
            if f.is_file():
                size = f.stat().st_size
                print(f"  - {f.name} ({size} bytes)")
        
        # Display contents
        print("\n" + "="*70)
        print("SkillNodeDto.java Content")
        print("="*70)
        print(skill_node_path.read_text(encoding='utf-8'))
        
        print("\n" + "="*70)
        print("PortfolioDNADto.java Content")
        print("="*70)
        print(portfolio_dna_path.read_text(encoding='utf-8'))
        
        print("\n" + "="*70)
        print("✓ TASK COMPLETED SUCCESSFULLY!")
        print("="*70)
        
        return True
    
    except Exception as e:
        print(f"✗ ERROR: {e}")
        import traceback
        traceback.print_exc()
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
