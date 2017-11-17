package Algorithm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Algorithm.FileProcessor.writeBytes;

/**
 * Created by Arsee on 08.11.2017.
 */
public class ReedMuller implements EncodeAlgorithm {
    private int MX[][];
    private int encodeMx[][];

    private void HadamardMatrix(int in) {
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
        for (int i = 0; i < MX.length; i++) {
            for (int j = 0; j < MX.length; j++) {

            }

        }

    }

    ReedMuller() {
        HadamardMatrix(4);
        encodeMx = new int[][]
                {{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1},
                        {0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1},
                        {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1},
                        {0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1, 0, 1}};

    }

    public void encode(String pathFrom, String pathTo) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(pathFrom));
            byte transofrmed[] = new byte[data.length * 8];
            for (int i = 0; i < data.length; i++) {
                int b = data[i];
                for (int j = 0; j < 7; j++) {
                    transofrmed[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) % 2);
                }
                if (b < 0) {
                    transofrmed[8 * i] = 1;
                }
            }
            byte ar2[] = this.encode(transofrmed);
            byte ar3[] = new byte[(int) Math.ceil(ar2.length / 1.0 / 8)];
            for (int i = 0; i < ar2.length; i += 8) {
                for (int j = 1; j < 8; j++) {
                    ar3[i / 8] += ar2[i + j] * Math.pow(2, 7 - j);
                }
                if (ar2[i] == 1) {
                    ar3[i / 8] *= -1;
                }
            }
            FileOutputStream fos = new FileOutputStream(pathTo);
            fos.write(ar3);
            fos.close();
        } catch (Exception e) {
            System.out.print("Failure of encoding\n" + e.getMessage() + "\n");
        }
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

    public File decode(File link) {
        byte[] bytes = Algorithm.FileProcessor.readBytes(link);
        byte[] transofrmation = new byte[bytes.length * 8];
        for (int i = 0; i < bytes.length; i++) {
            int b = bytes[i];
            for (int j = 0; j < 7; j++) {
                bytes[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) % 2);
            }
            if (b < 0) {
                bytes[8 * i] = 1;
            }
        }

        byte[] decoded = decode(transofrmation);
        byte result[] = new byte[(int) Math.floor(decoded.length / 1.0 / 8)];
        for (int i = 0; i < Math.floor(result.length / 1.0 / 8) * 8; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += decoded[i + j] * Math.pow(2, 7 - j);
            }
            if (decoded[i] == 1) {
                result[i / 8] *= -1;
            }
        }

        return writeBytes("decodedReedmuller.data", result);
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
}
