package Algorithm;

import java.io.*;

public class Repetition implements EncodeAlgorithm {

    int n;

    public Repetition(int n) {
        this.n = n;
    }

    public File encode(File input) {
        BufferedInputStream read = null;
        BufferedOutputStream write = null;
        File out = new File("outerf");
        try {
            out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            read = new BufferedInputStream(new FileInputStream(input));
            write = new BufferedOutputStream(new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int data;
        try {
            while ((data = read.read()) != -1) {
                for (int i = 0; i < n; i++) {
                    write.write((char) data);
                }
                write.flush();
            }
            read.close();
            write.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return out;
    }

    public static void main(String[] args) {
        Repetition r = new Repetition(5);
        File f = new File("file");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        f = r.encode(f);
        System.out.println(f.getAbsoluteFile());
    }

    public File decode(File in) {
        BufferedInputStream read = null;
        BufferedOutputStream write = null;
        File out = new File("outerfc");
        try {
            out.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            read = new BufferedInputStream(new FileInputStream(in));
            write = new BufferedOutputStream(new FileOutputStream(out));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int counter = 0;
        int[] data = new int[n];
        try {
            while ((data[0] = read.read()) != -1) {
                int zero = 0;
                int one = 0;
                if ((char) data[0] == '0') {
                    ++zero;
                } else {
                    ++one;
                }
                for (int i = 1; i < data.length; i++) {
                    data[i] = read.read();
                    if ((char) data[i] == '0') {
                        ++zero;
                    } else {
                        ++one;
                    }
                }
                if (zero > one) {
                    write.write(0);
                } else {
                    write.write(1);
                }
                write.flush();
            }
            write.close();
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out;
    }
}
