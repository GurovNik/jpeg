package Algorithm;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZW {

    private int size;
    private static final int initialDictionarySize = 256;
    private static final int initialSetAmoint = (int)Math.round(Math.log(initialDictionarySize)/Math.log(2)); //8
    private static int bits = initialSetAmoint;

    private int[] getInput() {
        return null;
    }

    private int getElement(int[] input, int index) {
        return input[index];
    }

    private boolean isInTable(String s) {
        return false;
    }

    public static List<Pair<Integer, Integer>> compress(String input) {
        List<Pair<Integer, Integer>> result = new ArrayList<Pair<Integer, Integer>>();
        Map<String, Integer> dictionary = new HashMap<>();
        //initializing dictionary and few variables for algorithm;
        for (char i = 0; i <= initialDictionarySize; i++) {
            String temp = "" + i;
            dictionary.put(temp, (int)i);
        }

        boolean flag = true;

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
        System.out.println(dictionary.size());
        return result;
    }

    public int[] decompress() {
        Map<Integer, String> dictionary = new HashMap<>();
        return null;
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
