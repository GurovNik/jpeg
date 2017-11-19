package Algorithm;

import java.io.File;

/**
 * Created by pivaso on 14.11.17.
 */
public interface CompressionAlgorithm {
    public File compress(File file);
    public File decompress(File file);
}
