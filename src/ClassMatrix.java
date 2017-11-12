
import java.io.IOException;

/**
 * Created by Nikita on 28/10/17.
 */


public class ClassMatrix {

    public static void main(String[] args) throws IOException {
        Service JPEG= new Service();
        JPEG.compress("/Users/Nikita/Desktop/2017-11-08 11.27.10.jpg");
        System.out.println("сжато");
        JPEG.decompress();
    }
}
