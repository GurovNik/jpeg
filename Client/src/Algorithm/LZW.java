package Algorithm;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import javafx.util.Pair;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class LZW {
    private int size;
    private final int initialDictionarySize = 256;
    private final int initialBitsAmount = (int)Math.round(Math.log(initialDictionarySize)/Math.log(2)) + 1; //9
    private int bits = initialBitsAmount;

    public List<Pair<Integer, Integer>> compress(String input) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        Map<String, Integer> dictionary = new HashMap<>();
        boolean flag = true;

        //initializing dictionary and few variables for algorithm;
        for (char i = 0; i <= initialDictionarySize; i++) {
            String temp = "" + i;
            dictionary.put(temp, (int)i);
        }

        for (int i = 0; (i < input.length()) && flag; i++) {
            String x = "" + input.charAt(i);
            //go as far as possible with familiar String
            while (dictionary.containsKey(x))
                if (i < input.length() - 1) {
                    i++;
                    x += input.charAt(i);
                    System.out.print(x);
                    System.out.print(" ");
                }
                else { //if end of input, then code of current string to the output
                    Pair<Integer, Integer> temp = new Pair<>(dictionary.get(x), bits);
                    result.add(temp);
                    flag = false;
                    break;
                }
            if (flag) {
                System.out.println(i);
                i--;
                dictionary.put(x, dictionary.size() - 1);
                if (dictionary.size() % 2 == 0) bits++;
                Pair<Integer, Integer> temp = new Pair<>(dictionary.get(x.substring(0, x.length() - 1)), bits);
                result.add(temp);
            }
        }

        return result;
    }

    public byte[] decompress(List<Integer> vals) {
        Map<Integer, List<Byte>> dictionary = loadByteDictionary();
        ArrayList<Byte> content = new ArrayList<>();
        int index = 0;
        int pW;
        int cW;
        List<Byte> wordPW;
        List<Byte> wordCW;
//        List<Byte> P;
//        List<Byte> C;

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
                for (Byte b: wordPW)
                    temp.add(b);
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

//        StringBuilder sb = new StringBuilder();
//        for (String s: content)
//            sb.append(s);

//        return sb.toString();
        byte bytes[] = new byte[content.size()];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = content.get(i);

        return bytes;
    }

    public File compress(File link) {
        byte bytes[] = null;
        try {
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
        BitSet bitSet = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bitSet.set(i * 8 + j, (bytes[i] & 1) > 0);
                bytes[i] <<= 1;
            }
        }
        return bitSet;
    }

    private Map<Integer, List<Byte>> loadByteDictionary() {
        Map<Integer, List<Byte>> dict = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ArrayList<Byte> list = new ArrayList<>();
            list.add((byte) i);
            dict.put(i, list);
        }

        return dict;
    }

    private List<Integer> getValues(BitSet bitSet) {
        ArrayList<Integer> values = new ArrayList<>();
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

        return values;
    }

    private File writeData(List<Pair<Integer, Integer>> data) {
        BitSet bitSets[] = new BitSet[data.size()];
        int index = 0;
        int bits_amount = 0;

        for (Pair p: data) {
            int value = (int)p.getKey();
            int bits = (int)p.getValue();
            bitSets[index++] = createBitSet(value, bits);
            bits_amount += bits;
        }

        BitSet bitSet = new BitSet(bits_amount);
        for (BitSet bs: bitSets)
            for (int j = 0; j < bs.length(); j++)
                bitSet.set(--bits_amount, bs.get(j));

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
        char aa = 0;
        char bb = 1;
        char cc = 2;
        String a = "" + aa;
        String b = "" + bb;
        String c = "" + cc;
        String input = "";
        List<Pair<Integer, Integer>> lzw = compress(a + b + b + c + a + c + a + b + b + a + b + b);
        for (int i = 0; i < lzw.size(); i++) {
            System.out.print(lzw.get(i).getKey());
            System.out.print(" ");
        }
    }
}
