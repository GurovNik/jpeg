package Huffman;

public class HuffmanNode {

    private char value;
    private int frequency;
    private HuffmanNode leftchild;
    private HuffmanNode rightchild;
    private HuffmanNode parent;

    public void setValue(char value) {
        this.value = value;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public void setLeftchild(HuffmanNode leftchild) {
        this.leftchild = leftchild;
    }

    public void setParent(HuffmanNode parent) {
        this.parent = parent;
    }

    public void setRightchild(HuffmanNode rightchild) {
        this.rightchild = rightchild;
    }

    public HuffmanNode getLeftchild() {
        return leftchild;
    }

    public HuffmanNode getParent() {
        return parent;
    }

    public HuffmanNode getRightchild() {
        return rightchild;
    }

    public void incrementFrequency(){
        frequency++;
    }

    public char getValue() {
        return value;
    }

    public int getFrequency() {
        return frequency;
    }
}
