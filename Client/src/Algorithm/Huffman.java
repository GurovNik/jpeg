package Algorithm;

import java.io.*;
import java.util.*;

public class Huffman implements CompressionAlgorithm {

    private String string;                                 // The string we are working with
    private HuffmanNode root;                              // The root of the huffman tree
    private String[][] table;                              // A table with symbols and their codewords

    // The constructor reads file and makes a string to work with
    public Huffman() {}

    // Decompresses a file and returns a result file
    public File decompress(File input) {
        byte inp[] = FileProcessor.readBytes(input);
        char arr[] = new char[inp.length];
        for (int i = 0; i < inp.length; i++) {
            arr[i] = (char) inp[i];
        }
        String code = new String(arr);
        StringBuilder result = new StringBuilder();
        StringBuilder codeword = new StringBuilder();
        int counter = 0;
        while (counter < code.length()){
            codeword.append(code.charAt(counter));
            for (int i = 0; i < table.length; i++){
                if (Objects.equals(table[i][1], codeword.toString())){
                    result.append(table[i][0]);
                    codeword = new StringBuilder();
                }
            }
            ++counter;
        }
        System.out.println("DECODING DONE!");
        String s = result.toString();
        s = s.replace(Character.toString(s.charAt(0)), "");
        s = s.replace(Character.toString(s.charAt(s.length() - 1)), "");
        byte outs[] = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            outs[i] = (byte) s.charAt(i);
        }
        return FileProcessor.writeBytes("huffmanCompressed.data", outs);
    }

    // Compresses an input file and returns a result file
    public File compress(File input) {
        byte inp[] = FileProcessor.readBytes(input);
        char arr[] = new char[inp.length];
        for (int i = 0; i < inp.length; i++) {
            arr[i] = (char) inp[i];
        }
        string = arr.toString();
        table = makeTable();
        StringBuilder result = new StringBuilder();
        for (int  i = 0; i < string.length(); i++){
            String s = "" + string.charAt(i);
            String code = "";
            for (int j = 0; j < table.length; j++){
                if (Objects.equals(table[j][0], s)){
                    code = table[j][1];
                    break;
                }
            }
            result.append(code);
        }
        String s = result.toString();
        s = s.replace(Character.toString(s.charAt(0)), "");
        s = s.replace(Character.toString(s.charAt(s.length() - 1)), "");
        byte outs[] = new byte[s.length()];
        for (int i = 0; i < s.length(); i++) {
            outs[i] = (byte) s.charAt(i);
        }
        return FileProcessor.writeBytes("huffmanCompressed.data", outs);
    }

    // Makes a table with symbols and their codewords
    String[][] makeTable() {
        PriorityQueue<HuffmanNode> queue = countFrequency();
        String[][] table = new String[queue.size()][2];
        root = makeTree(queue);
        makeCode(table, root, "");
        return table;
    }

    // Creates codewords for each symbol in the huffman tree
    void makeCode(String[][] table, HuffmanNode node, String currentCode) {
        if (node.getValue() != '\u0000'){
            int i = 0;
            for (int j = 0; j < table.length; j++){
                if (table[j][0] == null) {
                    i = j;
                    break;
                }
            }
            if (node.getParent() == null) currentCode = "0";
            String value = "" + node.getValue();
            table[i][0] = value;
            table[i][1] = currentCode;
        }
        if (node.getLeftchild() != null) makeCode(table, node.getLeftchild(), currentCode + "0");
        if (node.getRightchild() != null) makeCode(table, node.getRightchild(), currentCode + "1");
    }

    // Makes a tree of symbols with the most frequent in the root
    HuffmanNode makeTree(PriorityQueue<HuffmanNode> queue) {
        while (queue.size() > 1){
            HuffmanNode node1 = queue.poll();
            HuffmanNode node2 = queue.poll();
            HuffmanNode newNode = new HuffmanNode();
            newNode.setLeftchild(node1);
            newNode.setRightchild(node2);
            node1.setParent(newNode);
            node2.setParent(newNode);
            newNode.setFrequency(node1.getFrequency() + node2.getFrequency());
            queue.add(newNode);
        }
        return queue.peek();
    }

    // Counts the frequency of each symbol in the string and puts each symbol into the
    // priority queue, with the least frequent in the beginning
    PriorityQueue<HuffmanNode> countFrequency() {
        PriorityQueue<HuffmanNode> queue = new PriorityQueue<>(frequencyComparator);
        LinkedList<HuffmanNode> symbols = new LinkedList<>();
        for (int i = 0; i < string.length(); i++){
            char c = string.charAt(i);
            boolean exists = false;
            for (HuffmanNode node: symbols
                 ) {
                if (node.getValue() == c) {
                    exists = true;
                    node.incrementFrequency();
                }
            }
            if (!exists) {
                HuffmanNode node = new HuffmanNode();
                node.setValue(c);
                node.setFrequency(1);
                symbols.add(node);
            }
        }
        queue.addAll(symbols);
        return queue;
    }

    public static void main(String[] args) {
        Huffman huf = new Huffman();
        System.out.println("CODING!");
        File f = huf.compress(new File("input"));
        System.out.println("DECODING!");
        huf.decompress(f);
    }

    public String getString() {
        return string;
    }

    // A comparator for the priority queue based on frequency, the least frequent is the most prior
    public static Comparator<HuffmanNode> frequencyComparator = new Comparator<HuffmanNode>() {
        @Override
        public int compare(HuffmanNode o1, HuffmanNode o2) {
            return o1.getFrequency() - o2.getFrequency();
        }
    };

}
