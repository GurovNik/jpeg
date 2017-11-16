package Algorithm;

import javafx.util.Pair;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class LZW implements CompressionAlgorithm {
    private int size;
    private int bits;
    private final int initialDictionarySize = 256;
    private final int initialBitsAmount = (int)Math.round(Math.log(initialDictionarySize)/Math.log(2)) + 1; //9

    public LZW() {
        setUp();
    }

    private void setUp() {
        size = initialDictionarySize;
        bits = initialBitsAmount;
    }

    public List<Pair<Integer, Integer>> compress(byte input[]) {
        //initializing dictionary and few variables for algorithm;
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        Pair<Map<List<Byte>, Integer>, Map<Byte, List<Byte>>> maps = loadByteIntegerDictionary();
        Map<List<Byte>, Integer> dictionary = maps.getKey();
        Map<Byte, List<Byte>> backDictionary = maps.getValue();
        boolean flag = true;

        ArrayList<Byte> x = new ArrayList<>();
        for (int i = 0; (i < input.length) && flag; i++) {
            x.add(input[i]);
            //go as far as possible with familiar String
            while (dictionary.containsKey(x))
                if (i < input.length - 1) {
                    i++;
                    x.add(input[i]);
                }
                else { //if end of input, then code of current string to the output and end LZW coding
                    Pair<Integer, Integer> temp = new Pair<>(dictionary.get(x), bits);
                    result.add(temp);
                    flag = false;
                    break;
                }
            if (flag) { //flag for catching end of the lzw
                i--;
                dictionary.put(x, dictionary.size());
                if (dictionary.size() % Math.pow(2, bits) == 0)
                    bits++; //bits for decoding part of the LZW
                Pair<Integer, Integer> temp = new Pair<>(dictionary.get(backDictionary.get(x.get(0))), bits);
                result.add(temp);
            }
            x.clear();
        }

        return result;
    }

    public byte[] decompress(List<Integer> vals) {
        Map<Integer, List<Byte>> dictionary = loadIntegerByteDictionary();
        ArrayList<Byte> content = new ArrayList<>();
        int index = 0;
        int pW;
        int cW;
        List<Byte> wordPW;
        List<Byte> wordCW;

        cW = vals.get(index);
        wordCW = dictionary.get(cW);
        for (Byte b: wordCW)
            content.add(b);

        for (int i = 1; i < vals.size(); i++) {
            pW = cW;
            cW = vals.get(++index);
            if (dictionary.containsKey(cW)) {
                wordCW = dictionary.get(cW);
                for (Byte b: wordCW)
                    content.add(b);
                wordPW = dictionary.get(pW);

                ArrayList<Byte> temp = new ArrayList<>();
                for (int k = 0; k < wordPW.size(); k++) {
                    temp.add(wordPW.get(k));
                }
                temp.add(wordCW.get(0));
                dictionary.put(++size, temp);
            } else {
                wordPW = dictionary.get(pW);

                ArrayList<Byte> temp = new ArrayList<>();
                for (Byte b: wordPW)
                    temp.add(b);
                temp.add(wordPW.get(0));

                dictionary.put(++size, temp);
                for (Byte b: temp)
                    content.add(b);
            }
        }

        byte bytes[] = new byte[content.size()];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = content.get(i);

        return bytes;
    }

    public File compress(File link) {
        setUp();

        byte bytes[] = null;
        try {
            System.out.println(link.getAbsolutePath());
            bytes = Files.readAllBytes(link.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

//        BitSet bitSet = createBitSet(bytes);
//        ArrayList<String> values = fillList(values, bitSet);

        List<Pair<Integer, Integer>> phrases = compress(bytes);
        File out = writeData(phrases);

        return out;
    }

    public File decompress(File link) {
        setUp();

        byte bytes[] = null;
        try {
            bytes = Files.readAllBytes(link.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        BitSet bitSet = createBitSet(bytes);
        List<Integer> values = getValues(bitSet);

        byte data[] = decompress(values);

        return writeBytes("decompressedLZW.data", data);
    }

    private BitSet createBitSet(int value, int bits) {
        BitSet bitSet = new BitSet(bits);
        for (int i = 0; i < bits; i++) {
            bitSet.set(i, (value & (1 << i)) > 0);
        }

        return bitSet;
    }

    private BitSet createBitSet(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++)
            System.out.print(bytes[i] + " ");
        System.out.println();

        BitSet bitSet = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bitSet.set(i * 8 + j, (bytes[i] & (1 << j)) > 0);
            }
        }

        System.out.println(bitSet.cardinality());
        return bitSet;
    }

    private Map<Integer, List<Byte>> loadIntegerByteDictionary() {
        Map<Integer, List<Byte>> dict = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ArrayList<Byte> list = new ArrayList<>();
            list.add((byte) i);
            dict.put(i, list);
        }

        return dict;
    }

    private Pair<Map<List<Byte>, Integer>, Map<Byte, List<Byte>>> loadByteIntegerDictionary() {
        Map<List<Byte>, Integer> dict = new HashMap<>();
        Map<Byte, List<Byte>> reverseDict = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ArrayList<Byte> list = new ArrayList<>();
            Byte b = (byte) i;

            list.add(b);
            dict.put(list, i);
            reverseDict.put(b, list);
        }

        Pair<Map<List<Byte>, Integer>, Map<Byte, List<Byte>>> maps = new Pair<>(dict, reverseDict);
        return maps;
    }

    private List<Integer> getValues(BitSet bitSet) {
        ArrayList<Integer> values = new ArrayList<>();
        int index1 = 0;
        int index2 = bits;
        int bitsUsed = 0;
        int temp_size = size;

        while (bitsUsed < bitSet.length()) {
            bitsUsed += index2 - index1;
            BitSet temp = bitSet.get(index1, index2 - 1);
            if (++temp_size % Math.pow(2, bits) == 0)
                bits++;
            index1 = index2;
            index2 = index1 + bits;

            int value = convertBits(temp);
            values.add(value);
        }

        return values;
    }

    private File writeData(List<Pair<Integer, Integer>> data) {
        BitSet bitSets[] = new BitSet[data.size()];
        int index = 0;
        int bits_amount = 0;

        for (Pair p: data) {
            int value = p.getKey() != null ? (int)p.getKey(): 0;
            int bits = (int)p.getValue();
            bitSets[index++] = createBitSet(value, bits);
            bits_amount += bits;
        }

        int counter = 0;

        BitSet bitSet = new BitSet(bits_amount);
        for (int i = 0; i < data.size(); i++) {
            for (int j = 0; j < data.get(i).getValue(); j++) {
                bitSet.set(counter++, bitSets[i].get(j));
            }
        }

        return writeBytes("compressedLZW.data", bitSet.toByteArray());
    }

    private File writeBytes(String filename, byte bytes[]) {
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

    private int convertBits(BitSet bitSet) {
        int value = 0;
        for (int i = 0; i < bitSet.length(); i++) {
            value += bitSet.get(i) ? (1 << i) : 0;
        }

        return value;
    }

    public static void main(String[] args) {
        System.out.println("Hell");
        LZW lzw = new LZW();
        File in = new File("in.data");
        File compressed = lzw.compress(in);

        LZW lzw1 = new LZW();
        File out = lzw1.decompress(compressed);

    }
}
