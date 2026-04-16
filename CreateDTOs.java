import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CreateDTOs {
    public static void main(String[] args) throws Exception {
        // Change to the portfolio directory
        String currentDir = System.getProperty("user.dir");
        System.out.println("Current directory: " + currentDir);
        
        String dtoDir = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        
        // Create directory
        Path dtoDirPath = Paths.get(dtoDir);
        System.out.println("Creating directory: " + dtoDirPath);
        Files.createDirectories(dtoDirPath);
        System.out.println("Directory created successfully!");
        
        // Create SkillNodeDto.java
        String skillNodeDto = """
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
""";
        Files.write(dtoDirPath.resolve("SkillNodeDto.java"), skillNodeDto.getBytes());
        System.out.println("SkillNodeDto.java created successfully!");
        
        // Create PortfolioDNADto.java
        String portfolioDnaDto = """
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
""";
        Files.write(dtoDirPath.resolve("PortfolioDNADto.java"), portfolioDnaDto.getBytes());
        System.out.println("PortfolioDNADto.java created successfully!");
        System.out.println("All DTOs created successfully!");
    }
}
