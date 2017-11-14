package Huffman;

import java.util.*;

public class Huffman {

    private String string;
    private HuffmanNode root;
    private String[][] table;

    public Huffman(String s){
        string = s;
    }

    String decodeString(String code){
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
            counter++;
        }
        return result.toString();
    }

    String codeString(){
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
        return result.toString();
    }

    String[][] makeTable(){
        PriorityQueue<HuffmanNode> queue = countFrequency();
        String[][] table = new String[queue.size()][2];
        root = makeTree(queue);
        makeCode(table, root, "");
        return table;
    }

    void makeCode(String[][] table, HuffmanNode node, String currentCode){
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

    HuffmanNode makeTree(PriorityQueue<HuffmanNode> queue){
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

    PriorityQueue<HuffmanNode> countFrequency(){
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

    public static Comparator<HuffmanNode> frequencyComparator = new Comparator<HuffmanNode>() {
        @Override
        public int compare(HuffmanNode o1, HuffmanNode o2) {
            return o1.getFrequency() - o2.getFrequency();
        }
    };

}
