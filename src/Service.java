import java.io.IOException;

/**
 * Created by Nikita on 31/10/17.
 */
public class Service {
 public void compress(String path) throws IOException {
     Algorithm compression= new Algorithm();
     compression.create();
     compression.readImage(path);
     compression.runCompressing();
     compression.runDecompressing();


  }
}
