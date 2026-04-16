import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDtoDir {
    public static void main(String[] args) throws Exception {
        String path = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        Files.createDirectories(Paths.get(path));
        
        // Verify
        boolean exists = Files.exists(Paths.get(path));
        boolean isDir = Files.isDirectory(Paths.get(path));
        long count = Files.list(Paths.get(path)).count();
        
        if (exists && isDir) {
            System.out.println("✓ SUCCESS: Directory created at:");
            System.out.println("  " + path);
            System.out.println("✓ Directory is empty (" + count + " items)");
        } else {
            System.out.println("✗ FAILED: Directory creation failed");
        }
    }
}
