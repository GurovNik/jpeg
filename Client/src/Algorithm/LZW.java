package Algorithm;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LZW {

    private int bits;
    private int size = 255;
    private final int initialSetAmoint = 8;
    private final int initialDictionarySize = 255;
    private int[] getInput() {
        //
        return null;
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
        return null;
    }

    private Map<Integer, String> loadDictionary() {
        Map<Integer, String> dict = new HashMap<>();
        for (int i = 0; i < size + 1; i++) {
            char c = (char) i;
            dict.put(i, ""+c);
        }
        return dict;
    }

    public static void main(String[] args) {
        int values[] = new int[]{(int)'a', (int)'a', (int)'b', (int)'b', 257, 257, (int)'b', (int)'c', (int)'a', 263, (int)'c', (int)'c'};
        ArrayList<Integer> vals = new ArrayList<>();
        for (int a: values)
            vals.add(a);
        
        LZW lzw = new LZW();
        String s = lzw.decompress(vals);
        System.out.println(s);
    }

}
