#!/usr/bin/env python3
"""
Execute this script to create the DTO directory and files:
python C:\Users\jihen\Downloads\portfolio\standalone_dto_creator.py
"""

if __name__ == "__main__":
    import os
    import sys
    
    def main():
        # Base paths
        base = r"C:\Users\jihen\Downloads\portfolio\src\main\java\jihen\portfolio"
        dto_dir = os.path.join(base, "dto")
        
        # Verify parent exists
        if not os.path.isdir(base):
            print(f"ERROR: Base directory does not exist: {base}")
            return False
        
        # Create dto directory
        try:
            os.makedirs(dto_dir, exist_ok=True)
            print(f"✓ Directory created: {dto_dir}")
        except Exception as e:
            print(f"ERROR creating directory: {e}")
            return False
        
        # SkillNodeDto.java content
        skill_dto = """package jihen.portfolio.dto;

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
"""
        
        # PortfolioDNADto.java content
        portfolio_dto = """package jihen.portfolio.dto;

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
"""
        
        # Create files
        skill_path = os.path.join(dto_dir, "SkillNodeDto.java")
        dna_path = os.path.join(dto_dir, "PortfolioDNADto.java")
        
        try:
            with open(skill_path, 'w', encoding='utf-8') as f:
                f.write(skill_dto)
            print(f"✓ File created: {skill_path}")
        except Exception as e:
            print(f"ERROR creating SkillNodeDto: {e}")
            return False
        
        try:
            with open(dna_path, 'w', encoding='utf-8') as f:
                f.write(portfolio_dto)
            print(f"✓ File created: {dna_path}")
        except Exception as e:
            print(f"ERROR creating PortfolioDNADto: {e}")
            return False
        
        # Verification
        print("\n" + "="*70)
        print("VERIFICATION")
        print("="*70)
        
        print(f"✓ Directory exists: {dto_dir}")
        
        files = os.listdir(dto_dir)
        print(f"✓ Files in directory: {len(files)}")
        for f in sorted(files):
            fpath = os.path.join(dto_dir, f)
            size = os.path.getsize(fpath)
            print(f"  - {f} ({size} bytes)")
        
        # Display contents
        print("\n" + "="*70)
        print("SkillNodeDto.java Content")
        print("="*70)
        with open(skill_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print("\n" + "="*70)
        print("PortfolioDNADto.java Content")
        print("="*70)
        with open(dna_path, 'r', encoding='utf-8') as f:
            print(f.read())
        
        print("\n" + "="*70)
        print("✓ TASK COMPLETED SUCCESSFULLY!")
        print("="*70)
        
        return True
    
    try:
        success = main()
        sys.exit(0 if success else 1)
    except Exception as e:
        print(f"FATAL ERROR: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
