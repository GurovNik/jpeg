package FrontEnd;

import Algorithm.*;
import Algorithm.JPEG.JpegCompression;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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

    private static String foldersPath = "C:\\It project\\DATA";
    private static String foldersPath1 = "C:/cygwin64/small_data";
    private static File folders = new File(foldersPath);
    public static String compressed = "compressed_";
    public static String decompressed = "decompressed_";
    public static String encoded = "encoded_";
    public static String decoded = "decoded_";
    public static String format;

    private static  byte[] bytesToBits(byte[] data) {
        int b;  //temp variable
        //array for bits
        byte transformed[] = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            b = data[i];
            if (b==-128){
                byte m128[] ={1,0,0,0,0,0,0,0};
                for (int j=0;j<8;j++) {
                    transformed[8 * i + j]=m128[j];
                }
                continue;
            }
            for (int j = 0; j < 7; j++) {
                //extract bits from bytes
                transformed[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) % 2);
            }
            //if byte is negative then first bit 1
            if (b < 0) {
                transformed[8 * i] = 1;
            }
        }

        return transformed;
    }
    static Random  random = new Random();
    public static  byte [] makeNoise (byte[] b1, double chance){

        byte result [] = new byte[b1.length];
        for (int i=0;i<b1.length;i++){
            result[i] = b1[i];
            for (int j=0;j<8;j++){
                double val = random.nextDouble();
                if (val < chance){
                    result[i] = (byte)(result[i] ^ (1 <<j));
                }
            }
        }
        return result;
    }
    static int difInBits(byte[] b1,byte [] b2){

        int result = 0;
        byte bits1[] = bytesToBits(b1);
        byte bits2[] = bytesToBits(b2);
        for (int i = 0; i < Math.min(bits1.length,bits2.length); i++){
           if (bits1[i]!= bits2[i]){
               result++;
           }
           if (i<20){
               System.out.print(bits1[i]+" "+bits2[i]+"\n");
           }

        }
        return result;
    }

    public static void main(String[] args) throws IOException, ParseException {
        for (int i = 0; i < comprNames.length; i++)
            algName.put(compressionAlgorithms[i], comprNames[i]);
        for (int i = 0; i < encodeNames.length; i++)
            algName.put(encodeAlgorithms[i], encodeNames[i]);

        int success = 0;
        int counter = 0;
        int items_amount = 0;


        int index = 1;
        double chance =0.1;
        EncodeAlgorithm alg = encodeAlgorithms[index];
        File stats_compression_json = new File(foldersPath + "/../Statistics/" + algName.get(alg)+chance + ".data");
        FileWriter fw = new FileWriter(stats_compression_json, true);

            for (File folder: folders.listFiles()) {
                for (File subfolder: folder.listFiles()) {
                    File files[] = subfolder.listFiles();
                    format = getFilenameExtension(files[0]);
                    items_amount = 0;
                    for (File item : files) {
                        if (items_amount == 5)
                            break;
                        items_amount++;

                        System.out.printf("Before encoding :: ");
                        System.out.printf(item.getAbsolutePath());
                        JSONObject json = new JSONObject();
                        long time = System.currentTimeMillis();
                        File encoded = alg.encode(item);
                        time = System.currentTimeMillis() - time;
                        json.put("encoding_time", Long.toString(time));
                        byte noised[] = makeNoise(FileProcessor.readBytes(encoded),0.1);
                        int mistakes =0;
                        time = System.currentTimeMillis();
                        File decoded_f = alg.decode(FileProcessor.writeBytes("NewDecoded.data",noised));
                        time = System.currentTimeMillis() - time;
                        mistakes=difInBits(FileProcessor.readBytes(decoded_f), FileProcessor.readBytes(item));
                        json.put("decoding_time", Long.toString(time));

                        json.put("message", "");
                        json.put("format", format);
                        json.put("coding", Integer.toString(index));
                        json.put("initial_size", Long.toString(item.length()));
                        json.put("encoded_size", Long.toString(encoded.length()));
                        json.put("mistakes",Integer.toString(mistakes));
                        String newPath = foldersPath + "/../Statistics/" + algName.get(alg) + "/" + compressed + item.getName() + Double.toString(chance)+".data";
//                        String newPath = foldersPath + "/../Statistics/" + comprNames[2] + "/" + compressed + item.getName() + ".data";
                        boolean t = encoded.renameTo(new File(newPath));
                        if (t) {
                            success++;


                            System.out.printf(" :: Success :: ");
                        }
                        statisticsCompression.put(encoded.getName(), json);

                        fw = new FileWriter(stats_compression_json, true);
                        fw.write(json.toJSONString() + "\n");
                        fw.close();

                        counter++;
                        System.out.println(":: Done :: " + counter);
                    }
                }
            }

//        Scanner sc = new Scanner(stats_compression_json);
//        JSONParser parser = new JSONParser();
//        while (sc.hasNextLine()) {
//            JSONObject json = (JSONObject) parser.parse(sc.nextLine());
//
//        }
    }
}
