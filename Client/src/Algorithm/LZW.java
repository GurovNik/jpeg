package Algorithm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class LZW {

    private int bits;
    private int size = 256;
    private final int initialBitsAmoint = 9;
    private final int initialDictionarySize = 255;
    private int[] getInput() {
        //
        return null;
    }

    LZW() {
        bits = initialBitsAmoint;
        size = initialDictionarySize;
    }

    private int getElement(int[] input, int index) {
        return input[index];
    }

    private boolean isInTable(String s) {
        return false;
    }

    public int[] compress() {
        int[] input = getInput();
        int[] result = new int[1000];
        Map<String, Integer> dictionary = new HashMap<>();
        String x = "";
        String y = "";

        int codeX, codeY;
        for (int i = 0; i < input.length; i++) {
            codeX = getElement(input, i);

        }

        return null;
    }

    public String decompress(ArrayList<Integer> vals) {
        Map<Integer, String> dictionary = loadDictionary();
        ArrayList<String> content = new ArrayList<>();
        int index = 0;
        int pW;
        int cW;
        String wordPW;
        String wordCW;
        String P;
        String C;


        cW = vals.get(index);
        wordCW = dictionary.get(cW);
        content.add(wordCW);


        for (int i = 1; i < vals.size(); i++) {
            pW = cW;
            cW = vals.get(++index);
            if (dictionary.containsKey(cW)) {
                wordCW = dictionary.get(cW);
                content.add(wordCW);

                wordPW = dictionary.get(pW);

                P = wordPW;
                C = wordCW.substring(0, 1);
                dictionary.put(++size, P + C);
            } else {
                wordPW = dictionary.get(pW);

                P = wordPW;
                C = wordPW.substring(0, 1);
                dictionary.put(++size, P + C);
                content.add(P + C);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String s: content)
            sb.append(s);

        return sb.toString();
    }

    public File decompress(File link) {
        ArrayList<Integer> values = new ArrayList<>();
        byte bytes[] = null;
        try {
            bytes = Files.readAllBytes(link.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitSet bitSet = createBitSet(bytes);
        fillList(values, bitSet);

        String data = decompress(values);

        File decompressedData = writeData(data);
        return decompressedData;
    }

    private BitSet createBitSet(byte[] bytes) {
        BitSet bitSet = new BitSet(bytes.length * 8);
        createBitSet(bytes);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bitSet.set(i * 8 + j, (bytes[i] & 1) > 0);
                bytes[i] <<= 1;
            }
        }
        return bitSet;
    }

    private void fillList(ArrayList<Integer> values, BitSet bitSet) {
        int index1 = 0;
        int index2 = bits;
        int bitsUsed = 0;

        while (bitsUsed < bitSet.length()) {
            bitsUsed += index2 - index1;
            BitSet temp = bitSet.get(index1, index2);
            if (++size % 2 == 0)
                bits++;
            index1 = index2 + 1;
            index2 = index1 + bits;

            int value = convertBits(temp);
            values.add(value);
        }
    }

    private File writeData(String data) {
        byte bytes[] = data.getBytes();
        File link = new File("lzwDecompressed.data");

        try {
            FileOutputStream fos = new FileOutputStream(link);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }

    private int convertBits(BitSet bitSet) {
        int value = 0;
        for (int i = 0; i < bitSet.length(); i++) {
            value += bitSet.get(i) ? (1 << i) : 0;
        }

        return value;
    }

    private Map<Integer, String> loadDictionary() {
        Map<Integer, String> dict = new HashMap<>();
        for (int i = 0; i < size; i++) {
            char c = (char) i;
            dict.put(i, ""+c);
        }
        return dict;
    }

    public static void main(String[] args) {
//        int values[] = new int[]{(int)'a', (int)'a', (int)'b', (int)'b', 257, 257, (int)'b', (int)'c', (int)'a', 263, (int)'c', (int)'c'};
//        ArrayList<Integer> vals = new ArrayList<>();
//        for (int a: values)
//            vals.add(a);
//
        LZW lzw = new LZW();
//        String s = lzw.decompress(vals);
//        System.out.println(s);

        BitSet bs = new BitSet(4);
        bs.set(0, true);
        bs.set(1, true);
        bs.set(2, false);
        bs.set(3, true);

        int val = lzw.convertBits(bs);
        System.out.println(val);
    }

}
