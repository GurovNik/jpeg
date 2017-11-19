package FrontEnd;

import Algorithm.*;
import Algorithm.JPEG.JpegCompression;
import org.json.simple.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static Algorithm.FileProcessor.getFilenameExtension;

/**
 * Created by evger on 19-Nov-17.
 */
public class StatsCollector {
    private static CompressionAlgorithm[] compressionAlgorithms = new CompressionAlgorithm[]{new LZW(), new Huffman(), new JpegCompression()};
    private static String[] comprNames = new String[]{"LZW", "HUFFMAN", "JPEG"};
    private static EncodeAlgorithm[] encodeAlgorithms = new EncodeAlgorithm[]{new Repetition(3), new Repetition(5), new ReedMuller(), new Hamming()};
    private static String[] encodeNames = new String[]{"REPETITION3", "REPETITION5", "REEDMULLER", "HAMMING"};

    private static HashMap<Object, String> algName = new HashMap<>();
    private static HashMap<String, JSONObject> statisticsCompression = new HashMap<>();
    private static HashSet<JSONObject> statisticsFull = new HashSet<>();

    private static String foldersPath = "C:/cygwin64/home/evger/DATA";
    private static String foldersPath1 = "C:/cygwin64/small_data";
    private static File folders = new File(foldersPath);
    public static String compressed = "compressed_";
    public static String decompressed = "decompressed_";
    public static String encoded = "encoded_";
    public static String decoded = "decoded_";
    public static String format;

    public static void main(String[] args) throws IOException {
        for (int i = 0; i < comprNames.length; i++)
            algName.put(compressionAlgorithms[i], comprNames[i]);
        for (int i = 0; i < encodeNames.length; i++)
            algName.put(encodeAlgorithms[i], encodeNames[i]);

        int success = 0;
        int counter = 0;
        int items_amount = 0;


        int index = 2;
        File stats_compression_json = new File(foldersPath + "/../stats/compression" + algName.get(comprNames[index]) + ".data");
        FileWriter fw = new FileWriter(stats_compression_json, true);

//        for (int i = 0; i < compressionAlgorithms.length - 2; i++) {
            CompressionAlgorithm alg = compressionAlgorithms[index];
            for (File folder: folders.listFiles()) {
                for (File subfolder: folder.listFiles()) {
                    File files[] = subfolder.listFiles();
                    format = getFilenameExtension(files[0]);
                    items_amount = 0;
                    for (File item : files) {
                        if (items_amount == 5)
                            break;
                        items_amount++;

                        System.out.printf("Before compression :: ");
                        System.out.printf(item.getAbsolutePath());
                        JSONObject json = new JSONObject();
                        long time = System.currentTimeMillis();
                        File compressed = alg.compress(item);
                        time = System.currentTimeMillis() - time;

                        json.put("message", "");
                        json.put("format", format);
                        json.put("compression", Integer.toString(index));
                        json.put("compression_time", Long.toString(time));
                        json.put("initial_size", Long.toString(item.length()));
                        json.put("compressed_size", Long.toString(compressed.length()));
                        String newPath = foldersPath + "/../Statistics/" + algName.get(alg) + "/" + compressed + item.getName() + ".data";
//                        String newPath = foldersPath + "/../Statistics/" + comprNames[2] + "/" + compressed + item.getName() + ".data";
                        boolean t = compressed.renameTo(new File(newPath));
                        if (t) {
                            success++;
                            statisticsCompression.put(compressed.getName(), json);


                            System.out.printf(" :: Success :: ");
                        }

                        fw = new FileWriter(stats_compression_json, true);
                        fw.write(json.toJSONString() + "\n");
                        fw.close();

                        counter++;
                        System.out.println(":: Done :: " + counter);
                    }
                }
            }
//        }

//        Collection<JSONObject> values = statisticsCompression.values();

//        for (JSONObject json: values)
    }
}
