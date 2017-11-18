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
        try {
            compression.readImage(path);
            compression.runCompressing();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO :: return link
        return file;
    }
    public File decompress(File file) {
        //TODO :: USE FILE
        JPEG decompression = new JPEG();
        decompression.create();
        try {
            return decompression.runDecompressing();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
