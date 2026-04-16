import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SetupDTO {
    public static void main(String[] args) {
        try {
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
            
            // List directory contents
            System.out.println("\n✓ Directory contents:");
            File dir = new File(basePath);
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    System.out.println("  - " + f.getName());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
