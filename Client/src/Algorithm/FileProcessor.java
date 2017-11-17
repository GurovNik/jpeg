package Algorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Created by evger on 17-Nov-17.
 */
public class FileProcessor {
    public static byte[] readBytes(File link) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(link.toPath());
        } catch(IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    public static File writeBytes(String filename, byte[] bytes) {
        File link = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(link);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }
}
