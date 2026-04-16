import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SetupDTOFiles {
    public static void main(String[] args) throws Exception {
        String basePath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        
        // Create directory
        Files.createDirectories(Paths.get(basePath));
        System.out.println("✓ Created directory: " + basePath);
        
        // Create SkillNodeDto.java
        String skillNodeDto = "package jihen.portfolio.dto;\n\n" +
            "import lombok.*;\n\n" +
            "@Getter\n" +
            "@Setter\n" +
            "@AllArgsConstructor\n" +
            "@NoArgsConstructor\n" +
            "@Builder\n" +
            "public class SkillNodeDto {\n" +
            "    private Long id;\n" +
            "    private String name;\n" +
            "    private String category;\n" +
            "    private Boolean isTrendy;\n" +
            "    private String description;\n" +
            "    private Integer searchCount;\n" +
            "}\n";
        
        Files.write(Paths.get(basePath, "SkillNodeDto.java"), skillNodeDto.getBytes(StandardCharsets.UTF_8));
        System.out.println("✓ Created SkillNodeDto.java");
        
        // Create PortfolioDNADto.java
        String portfolioDNADto = "package jihen.portfolio.dto;\n\n" +
            "import lombok.*;\n" +
            "import java.util.Set;\n\n" +
            "@Getter\n" +
            "@Setter\n" +
            "@AllArgsConstructor\n" +
            "@NoArgsConstructor\n" +
            "@Builder\n" +
            "public class PortfolioDNADto {\n" +
            "    private Long portfolioId;\n" +
            "    private String title;\n" +
            "    private String bio;\n" +
            "    private Integer totalViews;\n" +
            "    private Integer followerCount;\n" +
            "    private Boolean isVerified;\n" +
            "    private Set<SkillNodeDto> skills;\n" +
            "    private Set<String> projectCategories;\n" +
            "    private String primaryFocus;\n" +
            "    private String expertise;\n" +
            "}\n";
        
        Files.write(Paths.get(basePath, "PortfolioDNADto.java"), portfolioDNADto.getBytes(StandardCharsets.UTF_8));
        System.out.println("✓ Created PortfolioDNADto.java");
        
        // Verify
        System.out.println("\n✓ Files created successfully!");
        System.out.println("Directory: " + basePath);
        System.out.println("Files:");
        new File(basePath).listFiles((d, n) -> {
            System.out.println("  - " + n);
            return true;
        });
    }
}
