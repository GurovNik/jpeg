import java.io.File;
import java.io.IOException;

/**
 * Created by Nikita on 31/10/17.
 */
public class JpegCompression implements CompressionAlgorithm {
    public void compress(File file) throws IOException {
        String path=file.getPath();
        Algorithm compression = new Algorithm();
        compression.create();
        compression.readImage(path);
        compression.runCompressing();

    }
    public File decompress() throws IOException {
        Algorithm decompression = new Algorithm();
        decompression.create();
       return decompression.runDecompressing();

    }
}
