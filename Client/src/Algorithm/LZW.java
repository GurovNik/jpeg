package Algorithm;

public class LZW {

    private int codingtable[][] = new int[2][500];
    private int decodingTable[][] = new int[2][500];

    private int[] getInput() {
        //
        return null;
    }

    private int getElement(int[] input, int index) {
        return input[index];
    }

    public int[] coding() {
        int[] input = getInput();
        int[] result = new int[1000];
        String x = "";
        String y = "";
        int codeX, codeY;
        for (int i = 0; i < input.length; i++) {
            codeX = getElement(input, i);

        }

        return null;
    }

    public int[] decoding() {
        return null;
    }
}
