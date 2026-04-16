import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DTOCreator {
    public static void main(String[] args) {
        try {
            String basePath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
            Path dtoDir = Paths.get(basePath);
            
            // Create directory
            Files.createDirectories(dtoDir);
            System.out.println("✓ Directory created: " + basePath + "\n");
            
            // SkillNodeDto content
            String skillContent = "package jihen.portfolio.dto;\n\n" +
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
            
            // Write SkillNodeDto
            Path skillPath = dtoDir.resolve("SkillNodeDto.java");
            Files.write(skillPath, skillContent.getBytes());
            System.out.println("✓ Created: " + skillPath);
            
            // PortfolioDNADto content
            String dnaContent = "package jihen.portfolio.dto;\n\n" +
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
            
            // Write PortfolioDNADto
            Path dnaPath = dtoDir.resolve("PortfolioDNADto.java");
            Files.write(dnaPath, dnaContent.getBytes());
            System.out.println("✓ Created: " + dnaPath + "\n");
            
            // Verify
            System.out.println("=".repeat(70));
            System.out.println("VERIFICATION");
            System.out.println("=".repeat(70));
            System.out.println("Directory exists: " + Files.isDirectory(dtoDir));
            System.out.println("SkillNodeDto.java exists: " + Files.exists(skillPath));
            System.out.println("PortfolioDNADto.java exists: " + Files.exists(dnaPath) + "\n");
            
            System.out.println("Directory contents:");
            Files.list(dtoDir).forEach(file -> {
                try {
                    long size = Files.size(file);
                    System.out.println("  " + file.getFileName() + " (" + size + " bytes)");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            
            // Display contents
            System.out.println("\n" + "=".repeat(70));
            System.out.println("FILE CONTENTS: SkillNodeDto.java");
            System.out.println("=".repeat(70));
            System.out.println(new String(Files.readAllBytes(skillPath)));
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("FILE CONTENTS: PortfolioDNADto.java");
            System.out.println("=".repeat(70));
            System.out.println(new String(Files.readAllBytes(dnaPath)));
            
            System.out.println("\n✓✓✓ SUCCESS - All files created and verified! ✓✓✓");
            
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
