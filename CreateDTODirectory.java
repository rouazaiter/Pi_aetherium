import java.nio.file.Files;
import java.nio.file.Paths;

public class CreateDTODirectory {
    public static void main(String[] args) throws Exception {
        String dtoPath = "C:\\Users\\jihen\\Downloads\\portfolio\\src\\main\\java\\jihen\\portfolio\\dto";
        Files.createDirectories(Paths.get(dtoPath));
        System.out.println("Directory created: " + dtoPath);
    }
}
