import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDtoFilesNow {
    public static void main(String[] args) {
        try {
            String dtoDir = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
            Files.createDirectories(Paths.get(dtoDir));
            System.out.println("Created directory: " + dtoDir);
            
            // Create SkillNodeDto.java
            String skillNodeContent = "package jihen.portfolio.dto;\n\n" +
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
            
            Files.write(Paths.get(dtoDir, "SkillNodeDto.java"), skillNodeContent.getBytes());
            System.out.println("Created: SkillNodeDto.java");
            
            // Create PortfolioDNADto.java
            String portfolioContent = "package jihen.portfolio.dto;\n\n" +
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
            
            Files.write(Paths.get(dtoDir, "PortfolioDNADto.java"), portfolioContent.getBytes());
            System.out.println("Created: PortfolioDNADto.java");
            
            System.out.println("\n=== VERIFICATION ===");
            System.out.println("Files in " + dtoDir + ":");
            File dir = new File(dtoDir);
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    System.out.println("  ✓ " + file.getName());
                }
            }
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("SkillNodeDto.java contents:");
            System.out.println("=".repeat(60));
            System.out.println(skillNodeContent);
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("PortfolioDNADto.java contents:");
            System.out.println("=".repeat(60));
            System.out.println(portfolioContent);
            
            System.out.println("\n✓ TASK COMPLETED SUCCESSFULLY!");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
