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
    private static int bits = initialSetAmoint; //for decoding part

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
                else { //if end of input, then code of current string to the output and end LZW coding
                    Pair<Integer, Integer> temp = new Pair<>(dictionary.get(x), bits);
                    result.add(temp);
                    flag = false;
                    break;
                }
            if (flag) { //flag for catching end of the lzw
                System.out.println(i);
                i--;
                dictionary.put(x, dictionary.size() - 1);
                if (dictionary.size() % 2 == 0) bits++; //bits for decoding part of the LZW
                Pair<Integer, Integer> temp = new Pair<>(dictionary.get(x.substring(0, x.length() - 1)), bits);
                result.add(temp);
            }
        }
        //testing:
        System.out.print("Dictionary size: ");
        System.out.println(dictionary.size());
        return result;
    }

    public static void main(String[] args) {
        List<Pair<Integer, Integer>> lzw = compress("String for test should be there");
        for (int i = 0; i < lzw.size(); i++) {
            System.out.print(lzw.get(i).getKey());
            System.out.print(" ");
        }
    }
}
