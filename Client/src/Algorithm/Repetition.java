package Algorithm;

import java.io.File;
import java.util.BitSet;

public class Repetition implements EncodeAlgorithm {

    int n;

    public Repetition(int n) {
        this.n = n;
    }

    public static void main(String[] args) {
        Repetition rep = new Repetition(5);
        System.out.println("ENCODE!");
        File f = rep.encode(new File("input"));
        System.out.println("DECODE!");
        rep.decode(f);
        System.out.println("Yay!");
    }

    public File encode(File input) {
        byte store[] = FileProcessor.readBytes(input);
        BitSet set = createBitSet(store);
        BitSet out = new BitSet(store.length * n);

        for (int i = 0; i < store.length * 8; ++i) {
            for (int j = 0; j < n; ++j) {
                out.set(i * n + j, set.get(i));
            }
        }

        byte output[] = out.toByteArray();

        return FileProcessor.writeBytes("repetitionCompressed.data", output);
    }

    private BitSet createBitSet(byte[] bytes) {
        BitSet bitSet = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bitSet.set(i * 8 + j, (bytes[i] & (1 << j)) > 0);
            }
        }

        return bitSet;
    }

    public File decode(File input) {
        byte store[] = FileProcessor.readBytes(input);
        BitSet set = createBitSet(store);
        int thresh = (int) Math.ceil((double) store.length * ((double) 8 / n * 1.0));
        BitSet out = new BitSet(thresh);
        int k = 0;

        for (int i = 0; i < thresh; ++i) {

            byte zero = 0;
            byte one = 0;
            for (int j = 0; j < n; ++j) {
                if (set.get(i * n + j)) {
                    ++one;
                } else {
                    ++zero;
                }
            }
            if (zero < one) {
                out.set(k, true);
            }
            ++k;
        }
        byte output[] = out.toByteArray();
        return FileProcessor.writeBytes("decodedRepetition.data", output);
    }

}
