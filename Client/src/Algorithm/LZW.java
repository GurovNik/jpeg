package Algorithm;

import javafx.util.Pair;
import java.io.File;
import java.util.*;

import static Algorithm.FileProcessor.writeBytes;

public class LZW implements CompressionAlgorithm {
    private int size;
    private int bits;
    private final int initialDictionarySize = 256;
    private final int initialBitsAmount = (int)Math.round(Math.log(initialDictionarySize)/Math.log(2)) + 1; //9

    public LZW() {}

    public File compress(File link) {
        setUp();

        byte bytes[] = Algorithm.FileProcessor.readBytes(link);
        List<Pair<Integer, Integer>> phrases = compress(bytes);
        File out = writeData(phrases);

        return out;
    }

    public File decompress(File link) {
        setUp();

        byte bytes[] = Algorithm.FileProcessor.readBytes(link);
        BitSet bitSet = createBitSet(bytes);
        List<Integer> values = getValues(bitSet, bytes.length*8);

        byte data[] = decompress(values);

        return writeBytes("decompressedLZW.data", data);
    }

    private void setUp() {
        size = initialDictionarySize;
        bits = initialBitsAmount;
    }

    public List<Pair<Integer, Integer>> compress(byte input[]) {
        //initializing dictionary and few variables for algorithm;
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        Map<List<Byte>, Integer> dictionary = loadByteIntegerDictionary();
        boolean flag = true;


        ArrayList<Byte> C = new ArrayList<>();
        ArrayList<Byte> pc = new ArrayList<>();
        ArrayList<Byte> P = new ArrayList<>(C);

        for (int i = 0; i < input.length & flag; i++) {
            C.add(input[i]);
            for (Byte b: P)
                pc.add(b);
            for (Byte b: C)
                pc.add(b);
            //go as far as possible with familiar String that already exists in the dictionary
            if (dictionary.containsKey(pc)) {
                P = new ArrayList<>(pc);
            } else {
                //if end of input, then code of current string to the output and end LZW coding
                //if we got new String that is not in the dictionary, then add it
                //to the dictionary, write down the code of already existed part in the output
                //and continue with new simbol, that stopped the familiar sequence
                Pair<Integer, Integer> temp = new Pair<>(dictionary.get(P), bits);
                result.add(temp);
                dictionary.put(new ArrayList(pc), dictionary.size());
                if (dictionary.size() % Math.pow(2, bits) == 0)
                    bits++; //bits for decoding part of the LZW
                P = new ArrayList<>(C);
            }
            pc.clear();
            C.clear();
        }

        Pair<Integer, Integer> temp = new Pair<>(dictionary.get(P), bits);
        result.add(temp);

        return result;
    }

    public byte[] decompress(List<Integer> vals) {
        //initializing values and dictionary (with all sequences with length 1)
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
            //algorithm in general is the following:
            //while we have new string in the dictionary, increase it
            //if new string is not in the dictionary, then add it to the dictionary and write code of
            //the last string that was in it before last adding action
            pW = cW;
            cW = vals.get(++index);
            if (dictionary.containsKey(cW)) {
                wordCW = dictionary.get(cW);
                for (Byte b: wordCW)
                    content.add(b);
                wordPW = dictionary.get(pW);

                ArrayList<Byte> temp = new ArrayList<>();
                for (int k = 0; k < wordPW.size(); k++) {
                    byte val = wordPW.get(k) == null ? 0x00: wordPW.get(k);
                    temp.add(val);
                }
                temp.add(wordCW.get(0));
                dictionary.put(size++, new ArrayList(temp));
            } else {
                wordPW = dictionary.get(pW);

                ArrayList<Byte> temp = new ArrayList<>();
                for (Byte b: wordPW)
                    temp.add(b);
                temp.add(wordPW.get(0));

                dictionary.put(size++, new ArrayList(temp));
                for (Byte b: temp)
                    content.add(b);
            }
        }

        byte bytes[] = new byte[content.size()];
        for (int i = 0; i < bytes.length; i++)
            bytes[i] = content.get(i);

        return bytes;
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
                bitSet.set(i * 8 + j, (bytes[i] & (1 << j)) > 0);
            }
        }

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

    private Map<List<Byte>, Integer> loadByteIntegerDictionary() {
        Map<List<Byte>, Integer> dict = new HashMap<>();
        for (int i = 0; i < size; i++) {
            ArrayList<Byte> list = new ArrayList<>();
            Byte b = (byte) i;
            list.add(b);
            dict.put(list, i);
        }

        return dict;
    }

    private List<Integer> getValues(BitSet bitSet, int length) {
        ArrayList<Integer> values = new ArrayList<>();
        int index1 = 0;
        int index2 = bits;
        int bitsUsed = 0;
        int temp_size = size;

        while (bitsUsed < length) {
            bitsUsed += index2 - index1;
            BitSet temp = bitSet.get(index1, index2);
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

        byte bytes[] = bitSet.toByteArray();
        return writeBytes("compressedLZW.data", bytes);
    }

    private int convertBits(BitSet bitSet) {
        int value = 0;
        for (int i = 0; i < bitSet.length(); i++) {
            value += bitSet.get(i) ? (1 << i) : 0;
        }

        return value;
    }


    /*public static void main(String[] args) {
        System.out.println("Hell");
        LZW lzw = new LZW();
        File in = new File("input.txt");
        File compressed = lzw.compress(in);

        LZW lzw1 = new LZW();
        File out = lzw1.decompress(compressed);

    }*/
    public static void main(String[] args) {
        Hamming coding = new Hamming();
        File f = new File("in.data");
        f = coding.encode(f);
        Hamming decoding = new Hamming();
        File fprev = decoding.decode(f);
    }
}