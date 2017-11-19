package Algorithm;

import sun.misc.IOUtils;
import sun.security.util.BitArray;

import java.io.*;
import java.nio.Buffer;
import java.util.*;

public class Huffman implements CompressionAlgorithm {

    private String string;                                 // The string we are working with
    private HuffmanNode root;                              // The root of the huffman tree
    private HashMap<String, Long> table;                   // A table with symbols and their codewords

    // The constructor reads file and makes a string to work with
    public Huffman() throws FileNotFoundException {
        byte mas[] = FileProcessor.readBytes(new File("input"));
        char arr[] = new char[mas.length];
        for (int i = 0; i < mas.length; i++) {
            arr[i] = (char) mas[i];
        }
        string = new String(arr);
        table = new HashMap<>();
    }

    // Decompresses a file and returns a result file
    public File decompress(File input) {
        byte[] code = FileProcessor.readBytes(input);
        HashMap<Long, String> hashMap = new HashMap<>();
        Set<String> set = table.keySet();
        String[] array = new String[set.size()];
        int k = 0;
        for (String t: set) {
            array[k] = t;
            ++k;
        }
        for (int i = 0; i < array.length; i++) {
            hashMap.put(table.get(array[i]), array[i]);
        }
//        BufferedReader rr = new BufferedReader(new StringReader(code));
        ArrayList<Byte> result = new ArrayList<>(1024);
        int counter = 0;
        int n = 8;
        while (counter < code.length){
            for (int i = 0; i < n; i++) {

            }
            long temp = code[counter];
            for (int i = 0; i < 64; i++) {
                long tmp =  temp & (long)Math.pow(2,i)-1;
                if(hashMap.containsKey(tmp)){
                    String s = hashMap.get(tmp);
                    byte[] temBytes = s.getBytes();
                    for (int j = 0; j < temBytes.length; j++) {
                        result.add(temBytes[j]);
                    }
                }
                --n;
            }
            counter+=8;
        }
        System.out.println("DECODING DONE!");
        String s = result.toString();
        return FileProcessor.writeBytes("huffmanDecompressed.data", s.getBytes());
    }

    // Compresses an input file and returns a result file
    public File compress(File input) {
        byte[] arr = FileProcessor.readBytes(input);

        string = new String(arr);
//        BufferedReader rr = new BufferedReader(new StringReader(string));
//        rr.lines().forEach(string1 -> string1.replaceAll("\b", "\\b"));

        makeTable();
        StringBuilder result = new StringBuilder();
        for (int  i = 0; i < string.length(); i++){
            String s = "" + string.charAt(i);
            int code = 0;
            if (table.containsKey(s)) {
                code = table.get(s);
            }
            result.append(code);
        }
        String s = result.toString();

        return FileProcessor.writeBytes("huffmanCompressed.data", s.getBytes());
    }

    // Makes a table with symbols and their codewords
    void makeTable() {
        PriorityQueue<HuffmanNode> queue = countFrequency();
        root = makeTree(queue);
        makeCode(table, root, 0L);
    }

    // Creates codewords for each symbol in the huffman tree
    void makeCode(HashMap<String, Long> table, HuffmanNode node, Long currentCode) {
        if (node.getValue() != '\u0000'){
            int i = 0;

            //            for (int j = 0; j < table.length; j++){
//                if (table[j][0] == null) {
//                    i = j;
//                    break;
//                }
//            }
            if (node.getParent() == null) currentCode = 0L;
            String value = "" + node.getValue();
            table.put(value, currentCode);
        }
        if (node.getLeftchild() != null) {
            currentCode = currentCode << 1;
            makeCode(table, node.getLeftchild(), currentCode);
        }
        if (node.getRightchild() != null){
            currentCode = ((currentCode << 1) | 1);
            makeCode(table, node.getRightchild(), currentCode);
        }
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
        try {
            Huffman huf = new Huffman();
            System.out.println("CODING!");
            File f = huf.compress(new File("input"));
            System.out.println("DECODING!");
            huf.decompress(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

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
