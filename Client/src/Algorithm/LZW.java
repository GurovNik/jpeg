package Algorithm;

import java.util.HashMap;
import java.util.Map;

public class LZW {

    private int bits;
    private int size;
    private final int initialSetAmoint = 8;
    private final int initialDictionarySize = 255;
    private int[] getInput() {
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

    public int[] decompress() {
        Map<Integer, String> dictionary = new HashMap<>();
        return null;
    }
}
