package Network;

import java.util.Scanner;

public class Hamming {

    static int log2(int x) {
        return (int) (Math.log((x)) / Math.log(2)); //log with the base 2, only integer value
    }

    static double log22(int x) {
        return (Math.log((x)) / Math.log(2));  //log with the base 2
    }

    static void insert(Integer[] array, int where, Integer what, int currentSize) {
        //function for inserting element in the array in the paticular place
        for (int i = currentSize; i > where; i--)
            array[i] = array[i - 1];
        array[where] = what;
    }

    static Integer[] hamming_coder(Integer[] input_byte) {
        //int ncb = log2(input_bytes.length) + 1; 4 = ncb everywhere
        Integer result[] = new Integer[12];
        for (int i = 0; i < 8; i++)
            result[i] = input_byte[i];
        int forInserting[] = new int[4];
        for (int i = 0; i < 4; i++) //4 = ncb
            insert(result, (int) Math.pow(2, i) - 1, 0, result.length - 4 + i); //4 = ncb
        for (int i = 0; i < 4; i++) { //4 = ncb
            int summ = 0;
            for (int j = (int) Math.pow(2, i) - 1; j < result.length; j += (int) Math.pow(2, i + 1))
                for (int k = 0; (k < (int) Math.pow(2, i)) && (j + k < result.length); k++)
                    summ += result[j + k];
            int x = (int) Math.pow(2, i) - 1;
            result[(int) (Math.pow(2, i) - 1)] = summ % 2;
        }
        return result;
    }

    static Integer[] hamming_decoder(Integer[] input) {
        int sum = 0;
        double eps = 0.00001;
        Integer copy_of_input[] = new Integer[input.length];
        for (int i = 0; i < input.length; i++)
            copy_of_input[i] = input[i];
        //all the operations will be continued only with the copy of the original coded array
        for (int i = 0; i < 4; i++) {
            //counting additional bits one more time (4 bits for coded sequance length 12);
            int summ = 0;
            for (int j = (int) Math.pow(2, i) - 1; j < copy_of_input.length; j += (int) Math.pow(2, i + 1))
                for (int k = 0; (k < (int) Math.pow(2, i)) && (j + k < copy_of_input.length); k++)
                    if (log22(k + j + 1) - log2(k + j + 1) > eps) summ += copy_of_input[j + k];
            int x = (int) Math.pow(2, i) - 1;
            copy_of_input[x] = summ % 2;
            if (copy_of_input[x] != input[x])
                sum += x + 1;
        }
        if (sum != 0)
            if (copy_of_input[sum - 1] == 0) copy_of_input[sum - 1] = 1;
            else copy_of_input[sum - 1] = 0;
        Integer[] result = new Integer[input.length - (int) ((log2(input.length)) + 1)];
        int j = 0;
        for (int i = 0; i < copy_of_input.length; i++)
            if (log22(i + 1) - log2(i + 1) > eps) {
                result[j] = copy_of_input[i];
                j++;
            }
        return result;
    }

   public static void main(String[] args) {
        Integer arr[] = new Integer[8];
        Scanner scan = new Scanner(System.in);
        for (int i = 0; i < 8; i++)
            arr[i] = scan.nextInt();
        Integer codedArr[] = new Integer[12];
        codedArr = hamming_coder(arr);
        for (int i = 0; i < codedArr.length; i++)
            System.out.print(codedArr[i]);
        System.out.println();
        Integer decoder[] = new Integer[8];
        decoder = hamming_decoder(codedArr);
        for (int i = 0; i < 8; i++)
            System.out.print(decoder[i]);
    }
}
