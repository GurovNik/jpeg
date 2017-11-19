package Algorithm;

import java.io.*;
import java.nio.file.Files;

public class Hamming {
    public static byte[] readBytes(File link) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(link.toPath());
        } catch(IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }



    public static File writeBytes(String filename, byte[] bytes) {
        File link = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(link);
            fos.write(bytes);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return link;
    }

    static byte log2(int x) {
        return (byte) (Math.log((x)) / Math.log(2)); //log with the base 2, only integer value
    }

    static double log22(int x) {
        return (Math.log((x)) / Math.log(2));  //log with the base 2
    }

    static void insert(byte[] array, byte where, byte what, int currentSize) {
        //function for inserting element in the array in the paticular place
        for (int i = currentSize; i > where; i--)
            array[i] = array[i - 1];
        array[where] = what;
    }

    private static byte[] code(byte[] input_byte) {
        int numberOfAddiditionalBits = 4;
        //for the message of length 8 we need additional 4 bits (trunc(log2(N)) + 1 in general, since we ooded 1 byte, this number is 4)
        byte result[] = new byte[8 + numberOfAddiditionalBits];
        for (int i = 0; i < 8; i++)
            result[i] = input_byte[i];
        byte forInserting[] = new byte[numberOfAddiditionalBits];
        //inserting additional bits in the array, initializing them with starting values (0)
        for (int i = 0; i < numberOfAddiditionalBits; i++)
            insert(result, (byte) (Math.pow(2, i) - 1), (byte) 0, result.length - numberOfAddiditionalBits + i);
        //calculating actual values of additional bits
        for (int i = 0; i < numberOfAddiditionalBits; i++) {
            byte summ = 0;
            for (int j = (int) Math.pow(2, i) - 1; j < result.length; j += (int) Math.pow(2, i + 1))
                for (int k = 0; (k < (int) Math.pow(2, i)) && (j + k < result.length); k++)
                    summ += result[j + k];
            byte x = (byte) (Math.pow(2, i) - 1);
            result[x] = (byte) (summ % 2);
        }
        return result;
    }

    public File encode(File link) {
        byte[] data = readBytes(link);
        System.out.print("\n");
        byte[] transformed = bytesToBits(data);
        byte[] encoded = new byte[transformed.length*3/2];
        for (int i = 0; i < transformed.length/8; i++) {
            byte[] temp = new byte[8];
            for (int j = 0; j < 8; j++)
                temp[j] = transformed[j + i*8];
            byte[] answer = code(temp);
            for (int j = 0; j < answer.length; j++)
                encoded[i * 12 + j] = answer[j];
        }
        byte result[] = encodedToResult(encoded);
        return writeBytes("encodedHamming.data", result);
    }

    private static byte[] decoding(byte[] input) {
        //for (int i = 0; i < input.length; i++)
        //  System.out.print(input[i]);
        //System.out.println();
        int numberOfAddiditionalBits = 4;
        //for the message of length 8 we need additional 4 bits (trunc(log2(N)) + 1 in general, since we ooded 1 byte, this number is 4)
        int sum = 0;
        double eps = 0.00001;
        byte copy_of_input[] = new byte[input.length];
        for (int i = 0; i < input.length; i++)
            copy_of_input[i] = input[i];
        //all the operations will be continued only with the copy of the original coded array
        for (int i = 0; i < numberOfAddiditionalBits; i++) {
            //counting additional bits one more time (4 bits from the coded sequence of length 12);
            int summ = 0;
            for (int j = (int) Math.pow(2, i) - 1; j < copy_of_input.length; j += (int) Math.pow(2, i + 1))
                for (int k = 0; (k < (int) Math.pow(2, i)) && (j + k < copy_of_input.length); k++)
                    if (log22(k + j + 1) - log2(k + j + 1) > eps) summ += copy_of_input[j + k];
            int x = (int) Math.pow(2, i) - 1;
            copy_of_input[x] = (byte) (summ % 2);
            if (copy_of_input[x] != input[x])
                sum += x + 1;
        }
        //checking for an error and fixing it (sum store summ of indexes of wrong additional bits)
        if (sum != 0 && sum < 13) {
            if (copy_of_input[sum - 1] == 0) copy_of_input[sum - 1] = 1;
            else copy_of_input[sum - 1] = 0;
        }
        byte[] result = new byte[input.length - ((log2(input.length)) + 1)];
        int j = 0;
        //throwing to the output all the digits except control digits that are not part of the original message
        for (int i = 0; i < copy_of_input.length; i++)
            if (log22(i + 1) - log2(i + 1) > eps) {
                result[j] = copy_of_input[i];
                j++;
            }
        return result;
    }

    public File decode(File link, byte[] correct) {
        byte[] bytes = readBytes(link);
        byte[] transformed = bytesToBits(bytes);
        byte[] decoded = new byte[transformed.length * 2 / 3]; //decoded should be in 1,5 times smaller (since coded message is less then decoded in 3/2 times)
        for (int i = 0; i < transformed.length/12; i++) {
            byte[] temp = new byte[12];
            for (int j = 0; j < 12; j++)
                temp[j] = transformed[j + i*12];
            byte[] answer = new byte[8];
            for (int z = 0; z < answer.length; z++)
                answer[z] = decoding(temp)[z];
            for (int j = 0; j < answer.length; j++)
                decoded[i * 8 + j] = answer[j];
        }
        byte[] correctDecoded = new byte[decoded.length];
        for (int i = 0; i < correctDecoded.length - 2; i++) {
            correctDecoded[i] = decoded[i];
            if (Math.abs(correctDecoded[i] - correct[i]) > 0)
                System.out.println("WTF1");
        }
        byte result[] = decodedToResult(decoded);
        return writeBytes("decodedHamming.data", result);
    }

    private static byte[] encodedToResult(byte[] bytes) { //this feature converts bits to bytes
        byte result[] = new byte[(int) Math.ceil(bytes.length / 1.0 / 8)];
        byte bytesExtended[] = new byte[bytes.length + 300];

        for (int i = 0; i < bytes.length; i++)
            bytesExtended[i] = bytes[i];
        for (int i = 0; i < bytes.length; i += 8) {
            for (int j = 1; j < 8; j++)
                result[i / 8] += bytesExtended[i + j] * Math.pow(2, 7 - j);
            if (bytes[i] == 1) {
                result[i / 8] *= -1;
            }
        }
        return result;
    }

    private static byte[] bytesToBits(byte[] data) { //converts bytes to bits
        int b;
        byte transofrmed[] = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            b = data[i];
            if (b == -128){
                byte m128[] ={1,0,0,0,0,0,0,0};
                for (int j=0;j < 8; j++) {
                    transofrmed[8 * i + j] = m128[j];
                }
                continue;
            }
            for (int j = 0; j < 7; j++)
                transofrmed[8 * i + 7 - j] = (byte) (((Math.abs(b)) >> j) & 0x1);
            if (b < 0)
                transofrmed[8 * i] = 1;
            else
                transofrmed[8 * i] = 0;
        }
        return transofrmed;
    }

    private static byte[] decodedToResult(byte[] decoded) { //converts bits in bytes
        double temp = Math.floor(decoded.length / 1.0 / 8);
        byte result[] = new byte[(int) temp];
        for (int i = 0; i < temp * 8; i += 8) {
            for (int j = 1; j < 8; j++) {
                result[i / 8] += decoded[i + j] * Math.pow(2, 7 - j);
            }
            if (decoded[i] == 1) {
                result[i / 8] *= -1;
                if (result[i/8] == 0){
                    result[i/8] =- 128;
                }
            }

        }

        return result;
    }

    public static void main(String[] args) {
        Hamming coding = new Hamming();
        Hamming decoding = new Hamming();
        File f = new File("in.data");
        byte[] data = readBytes(f);
        byte[] notTransformed = bytesToBits(data);
        File fprev = decoding.decode(coding.encode(f), notTransformed);
    }
}
