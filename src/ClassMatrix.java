
import java.io.IOException;

/**
 * Created by Nikita on 28/10/17.
 */


public class ClassMatrix {

    public static void main(String[] args) throws IOException {
        Service JPEG= new Service();
        JPEG.compress("/Users/Nikita/Desktop/artwork_mystery_symbols_symbol_2560x1440_artwallpaperhi.com-ConvertImage.tif");
        System.out.println("сжато");
        JPEG.decompress();




    }
}
