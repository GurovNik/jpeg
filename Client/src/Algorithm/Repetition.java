package Algorithm;

import java.io.File;
import java.util.BitSet;

/**
 * Class Repetition represents N repetition code
 * where N could be any integer. Take bits and
 * repeat it N times in file.
 *
 */
public class Repetition implements EncodeAlgorithm {

    /**
     * Number of repetitions.
     */
    private int N;

    /**
     * Constructor that specify n
     * @param n - numberof repetition.
     */
    public Repetition(int n) {
        this.N = n;
    }


//
//    public static void main(String[] args) {
//        Repetition rep = new Repetition(5);
//        System.out.println("ENCODE!");
//        File f = rep.encode(new File("input"));
//        System.out.println("DECODE!");
//        rep.decode(f);
//        System.out.println("Yay!");
//    }

    /**
     * Encoding file by repetition of each bit N times.
     * Writing encoded bits into new file.
     *
     * @param input - file for encoding
     * @return - new File with encoded bits.
     */
    public File encode(File input) {
        byte store[] = FileProcessor.readBytes(input);
        BitSet set = createBitSet(store);
        BitSet out = new BitSet(store.length * N);

        for (int i = 0; i < store.length * 8; ++i) {
            for (int j = 0; j < N; ++j) {
                out.set(i * N + j, set.get(i));
            }
        }

        byte output[] = out.toByteArray();

        return FileProcessor.writeBytes("repetitionCompressed.data", output);
    }


    /**
     * Helper function. Fill bitset with bits of given byte array.
     * @param bytes - array of bytes.
     * @return - BitSet with bits of byte array.
     */
    private BitSet createBitSet(byte[] bytes) {
        BitSet bitSet = new BitSet(bytes.length * 8);
        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                bitSet.set(i * 8 + j, (bytes[i] & (1 << j)) > 0);
            }
        }

        return bitSet;
    }


    /**
     * Decoding file taking the majority of bits out of N.
     *
     * @param input - file with encoded bytes.
     * @return - decoded file.
     */
    public File decode(File input) {
        byte store[] = FileProcessor.readBytes(input);
        BitSet set = createBitSet(store);
        // Calculating threshold her, to not make recalculation each loop iteration.
        int thresh = (int) Math.ceil((double) store.length * (8.0 / N));

        BitSet out = new BitSet(thresh);
        int k = 0;

        for (int i = 0; i < thresh; ++i) {

            byte zero = 0;
            byte one = 0;
            for (int j = 0; j < N; ++j) {
                if (set.get(i * N + j)) {
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
