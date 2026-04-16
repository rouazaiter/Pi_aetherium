import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CreateDtoStructure {
    public static void main(String[] args) throws IOException {
        // Create directory
        String dirPath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directory created: " + dirPath);
            } else {
                System.out.println("Failed to create directory");
                return;
            }
        } else {
            System.out.println("Directory already exists: " + dirPath);
        }
        
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
        
        File skillNodeFile = new File(dirPath, "SkillNodeDto.java");
        try (FileWriter fw = new FileWriter(skillNodeFile)) {
            fw.write(skillNodeContent);
            System.out.println("Created: " + skillNodeFile.getAbsolutePath());
        }
        
        // Create PortfolioDNADto.java
        String portfolioDnaContent = "package jihen.portfolio.dto;\n\n" +
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
        
        File portfolioDnaFile = new File(dirPath, "PortfolioDNADto.java");
        try (FileWriter fw = new FileWriter(portfolioDnaFile)) {
            fw.write(portfolioDnaContent);
            System.out.println("Created: " + portfolioDnaFile.getAbsolutePath());
        }
        
        // List directory contents
        System.out.println("\nDirectory contents:");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                System.out.println("  - " + file.getName());
            }
        }
        
        // Verify and show contents
        System.out.println("\n=== SkillNodeDto.java ===");
        System.out.println(new String(java.nio.file.Files.readAllBytes(skillNodeFile.toPath())));
        
        System.out.println("\n=== PortfolioDNADto.java ===");
        System.out.println(new String(java.nio.file.Files.readAllBytes(portfolioDnaFile.toPath())));
    }
}
