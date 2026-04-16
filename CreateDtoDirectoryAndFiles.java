import java.nio.file.*;
import java.io.IOException;

public class CreateDtoDirectoryAndFiles {
    public static void main(String[] args) throws IOException {
        // Create the dto directory
        Path dtoDir = Paths.get("C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto");
        Files.createDirectories(dtoDir);
        System.out.println("✓ Directory created: " + dtoDir);

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

        Path skillNodeFile = dtoDir.resolve("SkillNodeDto.java");
        Files.write(skillNodeFile, skillNodeContent.getBytes());
        System.out.println("✓ Created: SkillNodeDto.java");

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

        Path portfolioDnaFile = dtoDir.resolve("PortfolioDNADto.java");
        Files.write(portfolioDnaFile, portfolioDnaContent.getBytes());
        System.out.println("✓ Created: PortfolioDNADto.java");

        // List directory contents
        System.out.println("\n=== Directory Listing ===");
        Files.list(dtoDir).forEach(file -> {
            try {
                long size = Files.size(file);
                System.out.println(file.getFileName() + " (" + size + " bytes)");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // Display full contents of SkillNodeDto.java
        System.out.println("\n=== SkillNodeDto.java ===");
        System.out.println(new String(Files.readAllBytes(skillNodeFile)));

        // Display full contents of PortfolioDNADto.java
        System.out.println("\n=== PortfolioDNADto.java ===");
        System.out.println(new String(Files.readAllBytes(portfolioDnaFile)));
    }
}
