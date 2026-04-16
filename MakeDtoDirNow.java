import java.nio.file.Files;
import java.nio.file.Paths;

public class MakeDtoDirNow {
    public static void main(String[] args) {
        try {
            String dirPath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
            Files.createDirectories(Paths.get(dirPath));
            System.out.println("SUCCESS: Directory created at " + dirPath);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
