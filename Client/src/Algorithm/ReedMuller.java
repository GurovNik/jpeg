package Algorithm;

import java.io.File;

import static Algorithm.FileProcessor.writeBytes;

/**
 * Created by Arsee on 08.11.2017.
 */
public class ReedMuller implements EncodeAlgorithm {
    private int MX[][];
    private int encodeMx[][];

    private void hadamardMatrix(int in) {
        MX = new int[(int) Math.pow(2, in)][(int) Math.pow(2, in)];
        MX[0][0] = 1;
        MX[0][1] = 1;
        MX[1][0] = 1;
        MX[1][1] = -1;
        for (int i = 1; i < in; i++) {
            for (int j = 0; j < Math.pow(2, i); j++) {
                for (int f = 0; f < Math.pow(2, i); f++) {
                    MX[(int) Math.pow(2, i) + j][f] = MX[j][f];
                    MX[(int) Math.pow(2, i) + j][(int) Math.pow(2, i) + f] = -MX[j][f];
                    MX[j][(int) Math.pow(2, i) + f] = MX[j][f];
                }
            }
        }
    }

    public ReedMuller() {
        hadamardMatrix(4);
        encodeMx = new int[][]
                {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
                        {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1},
                        {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}};

    }

    public File decode(File link) {
        byte[] bytes = Algorithm.FileProcessor.readBytes(link);
        byte[] transofrmed = bytesToBits(bytes);
        byte[] decoded = decode(transofrmed);
        byte result[] = decodedToResult(decoded);

        return writeBytes("decodedReedmuller.data", result);
    }

    public File encode(File link) {
        byte[] data = FileProcessor.readBytes(link);
        byte[] transofrmed = bytesToBits(data);
        byte encoded[] = encode(transofrmed);
        byte result[] = encodedToResult(encoded);

        return writeBytes("encodedMuller.data", result);
    }

    private byte[] encodedToResult(byte[] bytes) {
        byte result[] = new byte[(int) Math.ceil(bytes.length / 1.0 / 8)];

        for (int i = 0; i < bytes.length; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += bytes[i + j] * Math.pow(2, 7 - j);
            }
            if (bytes[i] == 1) {
                result[i / 8] *= -1;
            }
        }

        return result;
    }

    private byte[] bytesToBits(byte[] data) {
        int b;
        byte transofrmed[] = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            b = data[i];
            for (int j = 0; j < 7; j++) {
                transofrmed[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) % 2);
            }
            if (b < 0) {
                transofrmed[8 * i] = 1;
            }
        }

        return transofrmed;
    }

    private byte[] encode(byte bytes[]) {
        byte ar[] = new byte[(int) Math.ceil(bytes.length / 1.0 / encodeMx.length) * encodeMx.length];
        for (int i = 0; i < bytes.length; i++) {
            ar[i] = bytes[i];
        }
        byte out[] = new byte[(int) Math.ceil(ar.length / 1.0 / encodeMx.length) * encodeMx[0].length];
        int ind = 0;

        for (int i = 0; i < (Math.ceil(ar.length / encodeMx.length)); i += 1) {
            for (int j = 0; j < encodeMx[0].length; j++) {
                byte sum = 0;
                for (int f = 0; f < encodeMx.length; f++) {
                    sum = (byte) (sum ^ (ar[encodeMx.length * i + f] * encodeMx[f][j]));
                }
                out[ind] = sum;
                ind++;
            }
        }

        return out;
    }


    private byte[] decodedToResult(byte[] decoded) {
        byte result[] = new byte[(int) Math.floor(decoded.length / 1.0 / 8)];
        for (int i = 0; i < Math.floor(decoded.length / 1.0 / 8) * 8; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += decoded[i + j] * Math.pow(2, 7 - j);
            }
            if (decoded[i] == 1) {
                result[i / 8] *= -1;
            }
        }

        return result;
    }

    private byte[] decode(byte bytes[]) {
        byte out[] = new byte[bytes.length / encodeMx[0].length * encodeMx.length];
        int ind = 0;
        for (int i = 0; i < bytes.length / (encodeMx[0].length); i++) {
            byte value[] = new byte[encodeMx[0].length];
            for (int f = 0; f < MX.length; f++) {
                byte sum = 0;
                for (int j = 0; j < MX[0].length; j++) {
                    sum += MX[f][j] * (2 * bytes[encodeMx[0].length * i + j] - 1);
                }
                value[f] = sum;
            }
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

            int x1 = ((1 + MX[in][8]) / 2) ^ ((1 + MX[in][0]) / 2);
            int x2 = ((1 + MX[in][4]) / 2) ^ ((1 + MX[in][0]) / 2);
            int x3 = ((1 + MX[in][2]) / 2) ^ ((1 + MX[in][0]) / 2);
            int x4 = ((1 + MX[in][1]) / 2) ^ ((1 + MX[in][0]) / 2);
            int x0 = bytes[i * 16];
            if (val > 0) {
                x0 = 1;
            } else {
                x0 = 0;
            }
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
//        ReedMuller rm = new ReedMuller();
//        File f = new File("in.data");
//        f = rm.encode(f);
//        ReedMuller mr = new ReedMuller();
//        mr.decode(f);
        LZW lzw = new LZW();
        ReedMuller rm = new ReedMuller();
        File f = lzw.compress(new File("img.jpg"));
        File g = rm.encode(f);

        LZW lzw1 = new LZW();
        ReedMuller mr = new ReedMuller();
        File k = mr.decode(g);
        lzw1.decompress(f);
    }
}
