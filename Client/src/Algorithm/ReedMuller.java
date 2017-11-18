package Algorithm;

import java.io.File;

import static Algorithm.FileProcessor.writeBytes;

/**
 * This algorithms is implementation of Reed-Muller coding with params r=1 and m=4
 */
public class ReedMuller implements EncodeAlgorithm {
    public static final int r = 1;
    public static final int m = 4;
    //encoding transforms 5 bits to 16
    public static final int K=5;
    public static final int N=16;
    //Generating matrix G
    private int decodeMx[][];
    //Hadamard matrix
    private int encodeMx[][];

    /**
     * @param n on this param depends size of the Hadamard matrix it will have 2^n rows and columns. n>=1
     *          Hadamard matrix consist of -1s and 1s. It is needed to decode messages.
     *          This matrix builds recursively.
     *          Base matrix H1=
     *          {{1,1}
     *          {1,-1}}
     *          Then Hn=
     *          {Hn-1| Hn-1}
     *          {Hn-1|-Hn-1}
     *          where | - concatenation
     */
    private void hadamardMatrix(int n) {
        //initialise matrix
        decodeMx = new int[(int) Math.pow(2, n)][(int) Math.pow(2, n)];
        //create base matrix
        decodeMx[0][0] = 1;
        decodeMx[0][1] = 1;
        decodeMx[1][0] = 1;
        decodeMx[1][1] = -1;
        for (int i = 1; i < n; i++) {
            for (int j = 0; j < Math.pow(2, i); j++) {
                for (int f = 0; f < Math.pow(2, i); f++) {
                    //copy upper-right part
                    decodeMx[(int) Math.pow(2, i) + j][f] = decodeMx[j][f];
                    //copy and change sign of lower-right part
                    decodeMx[(int) Math.pow(2, i) + j][(int) Math.pow(2, i) + f] = -decodeMx[j][f];
                    //copy lower-left part
                    decodeMx[j][(int) Math.pow(2, i) + f] = decodeMx[j][f];
                }
            }
        }
    }
    //Default constructor
    public ReedMuller() {
        //initialise Hadamard matrix with param m.
        hadamardMatrix(m);
        //matrix for encoding massages
        encodeMx = new int[][]
                //first row consist of 1s. Columns (except 1 char) is binary representation of the 0-15 numbers
                {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
                        {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1},
                        {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}};

    }

    /**
     *
     * @param link file to decode
     * @return decoded file
     */
    public File decode(File link) {
        //read file as array of bytes
        byte[] bytes = Algorithm.FileProcessor.readBytes(link);
        //decompose bytes to bits
        byte[] transformed = bytesToBits(bytes);
        //decode the bits
        byte[] decoded = decode(transformed);
        //compose these bits to bytes
        byte result[] = decodedToResult(decoded);
        //create file from bytes and return it
        return writeBytes("decodedReedmuller.data", result);
    }

    /**
     *
     * @param link file to encode
     * @return encoded file
     */
    public File encode(File link) {
        //read file as array of the bytes
        byte[] data = FileProcessor.readBytes(link);
        //decompose bytes to bits
        byte[] transformed = bytesToBits(data);
        //encode the bits
        byte encoded[] = encode(transformed);
        //compose bytes from these bits
        byte result[] = encodedToResult(encoded);
        //create file from bytes and return it
        return writeBytes("encodedMuller.data", result);
    }

    /**
     *
     * @param bytes array of bytes consist of 0s and 1s
     * @return transformed bits to bytes array
     */
    private byte[] encodedToResult(byte[] bytes) {
        //initialise array with ceil in order to don't lose bits
        byte result[] = new byte[(int) Math.ceil(bytes.length / 1.0 / 8)];
        //transform bits to bytes
        for (int i = 0; i < bytes.length; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += bytes[i + j] * Math.pow(2, 7 - j);
            }
            //sign of byte
            if (bytes[i] == 1) {
                result[i / 8] *= -1;
            }
        }

        return result;
    }

    /**
     *
     * @param data array of bytes  in range of -128 and 127
     * @return array of bytes which consist of 0s and 1s
     */
    private byte[] bytesToBits(byte[] data) {
        int b;  //temp variable
        //array for bits
        byte transformed[] = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            b = data[i];
            for (int j = 0; j < 7; j++) {
                //extract bits from bytes
                transformed[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) % 2);
            }
            //if byte is negative then first bit 1
            if (b < 0) {
                transformed[8 * i] = 1;
            }
        }

        return transformed;
    }

    /**
     *
     * @param bytes array of bits (bytes have to contain 0s and 1s)
     * @return array of bytes (in range of -128 and 127)
     */
    private byte[] decodedToResult(byte[] bytes) {
        byte result[] = new byte[(int) Math.floor(bytes.length / 1.0 / 8)];
        for (int i = 0; i < Math.floor(result.length / 1.0 / 8) * 8; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += bytes[i + j] * Math.pow(2, 7 - j);
            }
            if (bytes[i] == 1) {
                result[i / 8] *= -1;
            }
        }
        return result;
    }
    /**
     *
     * @param bytes take sequence of bits (bytes have to consist of 0s and 1s)
     *              each 5 bits will be encoded to 16
     * @return encoded array of bits
     */
    private byte[] encode(byte bytes[]) {
        //initialise new array of bits in order length of array is multiple of 5
        byte ar[] = new byte[(int) Math.ceil(bytes.length / 1.0 / K) *K];
        //copy initial array
        for (int i = 0; i < bytes.length; i++) {
            ar[i] = bytes[i];
        }
        //initialise out array with length |array|/K*N
        byte out[] = new byte[(int) Math.ceil(ar.length / 1.0 / K) * N];
        int ind = 0;
        //take each block with length =5
        for (int i = 0; i < (Math.ceil(ar.length / K)); i += 1) {
            //Multiply codeword by encoding matrix
            for (int j = 0; j < N; j++) {
                byte sum = 0;
                for (int f = 0; f < K; f++) {
                    sum = (byte) (sum ^ (ar[K * i + f] * encodeMx[f][j]));
                }
                //save result to out
                out[ind] = sum;
                ind++;
            }
        }

        return out;
    }

    /**
     *
     * @param bytes accept array of bits
     *              //each 16 bits will be decoded to 5 bits
     * @return decoded massage
     */
    private byte[] decode(byte bytes[]) {
        //initialise new array with length |bytes|/16*5
        byte out[] = new byte[bytes.length / N *K];
        int ind = 0;
        //for each block with length = 16
        for (int i = 0; i < bytes.length / (N); i++) {
            //will contain product of codeword and decode matrix
            byte value[] = new byte[N];
            //product of codeword and decode matrix
            for (int f = 0; f < N; f++) {
                byte sum = 0;
                for (int j = 0; j < N; j++) {
                    sum += decodeMx[f][j] * (2 * bytes[N* i + j] - 1);
                }
                //save the product
                value[f] = sum;
            }
            //now we need find index of max on absolute value component of value array
            int max = 0;
            int val = 0;
            int in = 0;
            for (int j = 0; j < value.length; j++) {
                if (max < Math.abs(value[j])) {
                    max = Math.abs(value[j]);
                    val = value[j];
                    in = j;
                }
            }
            //decoding according to the Reed-Muller algorithm
            //if value of component is negative then x0=0 else x0=1
            //X1 = Y8^Y0
            //X2 =Y4^Y0
            //x3 = Y2^Y0
            //X4 = Y1^Y0
            int x1 = ((1 + decodeMx[in][8]) / 2) ^ ((1 + decodeMx[in][0]) / 2);
            int x2 = ((1 + decodeMx[in][4]) / 2) ^ ((1 + decodeMx[in][0]) / 2);
            int x3 = ((1 + decodeMx[in][2]) / 2) ^ ((1 + decodeMx[in][0]) / 2);
            int x4 = ((1 + decodeMx[in][1]) / 2) ^ ((1 + decodeMx[in][0]) / 2);
            int x0;
            if (val > 0) {
                x0 = 1;
            } else {
                x0 = 0;
            }
            //save to out array
            out[ind] = (byte) x0;
            out[ind + 1] = (byte) x1;
            out[ind + 2] = (byte) x2;
            out[ind + 3] = (byte) x3;
            out[ind + 4] = (byte) x4;
            ind += 5;


        }
        return out;
    }

    public static void main(String[] args) {
        ReedMuller rm = new ReedMuller();
        File f = new File("in.data");
        f = rm.encode(f);
        ReedMuller mr = new ReedMuller();
        mr.decode(f);
    }
}
