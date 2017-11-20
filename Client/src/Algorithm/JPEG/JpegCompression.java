package Algorithm.JPEG;

import Algorithm.CompressionAlgorithm;
import java.io.File;
import java.io.IOException;

/**
 * Created by Nikita on 31/10/17.
 */
public class JpegCompression implements CompressionAlgorithm {

    public File compress(File file) {
        String path=file.getPath();
        JPEG compression = new JPEG();
        compression.create();
        File link = null;
        try {
            compression.readImage(path);
            link = compression.runCompressing();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }
    public File decompress(File file) {
        JPEG decompression = new JPEG();
        decompression.create();
        try {
            return decompression.runDecompressing(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
