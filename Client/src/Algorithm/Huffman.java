package Algorithm;


import java.io.*;
import java.util.*;

public class Huffman implements CompressionAlgorithm {

    private String string;                                 // The string we are working with
    private HuffmanNode root;                              // The root of the huffman tree
    private HashMap<String, String> table;                   // A table with symbols and their codewords

    // The constructor reads file and makes a string to work with
    public Huffman() throws FileNotFoundException {
    }

    // Decompresses a file and returns a result file
    public File decompress(File input) {
        Scanner scan = null;
        try {
            scan = new Scanner(input);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String code = scan.nextLine();
        StringBuilder result = new StringBuilder();
        StringBuilder codeword = new StringBuilder();
        int counter = 0;
        HashMap<String, String> hash = new HashMap<>();
        Set<String> keys = table.keySet();
        for (String k: keys) {
            hash.put(table.get(k), k);
        }
        while (counter < code.length()){
            codeword.append(code.charAt(counter));
            if(hash.containsKey(codeword.toString())){
                result.append(hash.get(codeword.toString()));
                codeword = new StringBuilder();
            }
            ++counter;
        }
        System.out.println("DECODING DONE!");


        String s = result.toString();
        System.out.println(s);
        byte outs[] = Base64.getDecoder().decode(s);
        return FileProcessor.writeBytes("huffmanDecompressed.data", outs);
    }

    // Compresses an input file and returns a result file
    public File compress(File input) {
        File result = new File("compressedHuffman.data");
        string = Base64.getEncoder().encodeToString(FileProcessor.readBytes(input));

        System.out.println(string);
        table = new HashMap<>();
        makeTable();
        FileWriter fw = null;
        try {
            fw = new FileWriter(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < string.length(); i++) {
            String s = "" + string.charAt(i);
            try {
            if (table.containsKey(s)) {
                fw.write(table.get(s));
            }
                fw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Makes a table with symbols and their codewords
    void makeTable() {
        PriorityQueue<HuffmanNode> queue = countFrequency();
        root = makeTree(queue);
        makeCode(table, root, "0");
    }

    // Creates codewords for each symbol in the huffman tree
    void makeCode(HashMap<String, String> table, HuffmanNode node, String currentCode) {
        if (node.getValue() != '\u0000'){
            if (node.getParent() == null) currentCode = "0";
            String value = "" + node.getValue();
            table.put(value, currentCode);
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
//        Huffman huf = new Huffman();
//        System.out.println("CODING!");
//        File f = huf.compress(new File("input"));
//        System.out.println("DECODING!");
//        huf.decompress(f);
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
