import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDtoFilesNow {
    public static void main(String[] args) {
        String dirPath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        
        // Create directory
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Directory created: " + dirPath);
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
        
        String skillNodePath = dirPath + "\\SkillNodeDto.java";
        try {
            FileWriter writer = new FileWriter(skillNodePath);
            writer.write(skillNodeContent);
            writer.close();
            System.out.println("File created: " + skillNodePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Create PortfolioDNADto.java
        String portfolioDNAContent = "package jihen.portfolio.dto;\n\n" +
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
        
        String portfolioDNAPath = dirPath + "\\PortfolioDNADto.java";
        try {
            FileWriter writer = new FileWriter(portfolioDNAPath);
            writer.write(portfolioDNAContent);
            writer.close();
            System.out.println("File created: " + portfolioDNAPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Verify
        System.out.println("\n=== VERIFICATION ===");
        System.out.println("Directory exists: " + dir.exists());
        System.out.println("\nFiles in directory:");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                System.out.println("  - " + f.getName());
            }
        }
        
        // Display contents
        System.out.println("\n=== FILE: SkillNodeDto.java ===");
        try {
            System.out.println(new String(Files.readAllBytes(Paths.get(skillNodePath))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("\n=== FILE: PortfolioDNADto.java ===");
        try {
            System.out.println(new String(Files.readAllBytes(Paths.get(portfolioDNAPath))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
