import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDtoFiles {
    public static void main(String[] args) {
        try {
            String basePath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
            File dtoDir = new File(basePath);
            
            // Create directory
            if (!dtoDir.exists()) {
                dtoDir.mkdirs();
                System.out.println("✓ Directory created: " + basePath);
            } else {
                System.out.println("✓ Directory already exists: " + basePath);
            }
            
            // Create SkillNodeDto.java
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
            
            String skillPath = basePath + "\\SkillNodeDto.java";
            try (FileWriter fw = new FileWriter(skillPath)) {
                fw.write(skillContent);
                System.out.println("✓ Created: " + skillPath);
            }
            
            // Create PortfolioDNADto.java
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
            
            String dnaPath = basePath + "\\PortfolioDNADto.java";
            try (FileWriter fw = new FileWriter(dnaPath)) {
                fw.write(dnaContent);
                System.out.println("✓ Created: " + dnaPath);
            }
            
            // Verification
            System.out.println("\n" + "=".repeat(70));
            System.out.println("VERIFICATION");
            System.out.println("=".repeat(70));
            System.out.println("Directory exists: " + dtoDir.isDirectory());
            System.out.println("SkillNodeDto.java exists: " + new File(skillPath).isFile());
            System.out.println("PortfolioDNADto.java exists: " + new File(dnaPath).isFile());
            
            System.out.println("\nDirectory contents:");
            for (File f : dtoDir.listFiles()) {
                System.out.println("  " + f.getName() + " (" + f.length() + " bytes)");
            }
            
            // Display file contents
            System.out.println("\n" + "=".repeat(70));
            System.out.println("FILE: SkillNodeDto.java");
            System.out.println("=".repeat(70));
            System.out.println(new String(Files.readAllBytes(Paths.get(skillPath))));
            
            System.out.println("\n" + "=".repeat(70));
            System.out.println("FILE: PortfolioDNADto.java");
            System.out.println("=".repeat(70));
            System.out.println(new String(Files.readAllBytes(Paths.get(dnaPath))));
            
            System.out.println("\n✓✓✓ SUCCESS - All files created and verified! ✓✓✓");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
