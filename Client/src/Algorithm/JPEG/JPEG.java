/**
 * Created by Nikita on 28/10/17.
 */
package Algorithm.JPEG;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class JPEG {
    private RealMatrix U;       //DCT matrix
    private RealMatrix Utransposed;
    private RealMatrix Z;       //cuantization matrix
    private BufferedImage image = null;     //initial picture
    private int height;                     //picture size
    private int width;                     //picture size
    private final int LIMIT = 3500000;           // limit of size for big pictures
    private final int LOWLIMIT = 500000;           // limit of size for small pictures
    private int SIZE;
    private File outputFile;            // decompressed file for reciever

    public void create() {              // creates all needed instruments
        double[][] Umatrix = {          //this is default matrix for DCT transformation
                {Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4, Math.sqrt(2) / 4},
                {Math.cos(Math.PI / 16) / 2, Math.cos(3 * Math.PI / 16) / 2, Math.cos(5 * Math.PI / 16) / 2, Math.cos(7 * Math.PI / 16) / 2, Math.cos(9 * Math.PI / 16) / 2, Math.cos(11 * Math.PI / 16) / 2, Math.cos(13 * Math.PI / 16) / 2, Math.cos(15 * Math.PI / 16) / 2},
                {Math.cos(2 * Math.PI / 16) / 2, Math.cos(6 * Math.PI / 16) / 2, Math.cos(10 * Math.PI / 16) / 2, Math.cos(14 * Math.PI / 16) / 2, Math.cos(18 * Math.PI / 16) / 2, Math.cos(22 * Math.PI / 16) / 2, Math.cos(26 * Math.PI / 16) / 2, Math.cos(30 * Math.PI / 16) / 2},
                {Math.cos(3 * Math.PI / 16) / 2, Math.cos(9 * Math.PI / 16) / 2, Math.cos(15 * Math.PI / 16) / 2, Math.cos(21 * Math.PI / 16) / 2, Math.cos(27 * Math.PI / 16) / 2, Math.cos(33 * Math.PI / 16) / 2, Math.cos(39 * Math.PI / 16) / 2, Math.cos(45 * Math.PI / 16) / 2},
                {Math.cos(4 * Math.PI / 16) / 2, Math.cos(12 * Math.PI / 16) / 2, Math.cos(20 * Math.PI / 16) / 2, Math.cos(28 * Math.PI / 16) / 2, Math.cos(36 * Math.PI / 16) / 2, Math.cos(44 * Math.PI / 16) / 2, Math.cos(52 * Math.PI / 16) / 2, Math.cos(60 * Math.PI / 16) / 2},
                {Math.cos(5 * Math.PI / 16) / 2, Math.cos(15 * Math.PI / 16) / 2, Math.cos(25 * Math.PI / 16) / 2, Math.cos(35 * Math.PI / 16) / 2, Math.cos(45 * Math.PI / 16) / 2, Math.cos(55 * Math.PI / 16) / 2, Math.cos(65 * Math.PI / 16) / 2, Math.cos(75 * Math.PI / 16) / 2},
                {Math.cos(6 * Math.PI / 16) / 2, Math.cos(18 * Math.PI / 16) / 2, Math.cos(30 * Math.PI / 16) / 2, Math.cos(42 * Math.PI / 16) / 2, Math.cos(54 * Math.PI / 16) / 2, Math.cos(66 * Math.PI / 16) / 2, Math.cos(78 * Math.PI / 16) / 2, Math.cos(90 * Math.PI / 16) / 2},
                {Math.cos(7 * Math.PI / 16) / 2, Math.cos(21 * Math.PI / 16) / 2, Math.cos(35 * Math.PI / 16) / 2, Math.cos(49 * Math.PI / 16) / 2, Math.cos(63 * Math.PI / 16) / 2, Math.cos(77 * Math.PI / 16) / 2, Math.cos(91 * Math.PI / 16) / 2, Math.cos(105 * Math.PI / 16) / 2},
        };
        double[][] Zmatrix = {      // default matrix for cuantization
                {16, 11, 10, 16, 24, 40, 51, 61},
                {12, 12, 14, 19, 26, 58, 60, 55},
                {14, 13, 16, 24, 40, 57, 69, 56},
                {14, 17, 22, 29, 51, 87, 80, 62},
                {18, 22, 37, 56, 68, 109, 103, 77},
                {24, 35, 55, 64, 81, 104, 113, 92},
                {49, 64, 78, 87, 103, 121, 120, 101},
                {72, 92, 95, 98, 112, 100, 103, 99},
        };
        U = MatrixUtils.createRealMatrix(Umatrix);
        Utransposed = U.transpose();
        Z = MatrixUtils.createRealMatrix(Zmatrix);

    }

    public void readImage(String path) throws IOException {         //reads file
        File f = null;
        try {
            f = new File(path); //image file path
            image = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("Error: " + e);
        }

    }

    public File runCompressing() throws IOException {   // start procedure of compressing
        return preProcessing();
    }

    public File runDecompressing(File file) throws IOException {   // start procedure of decompressing
        decomposition(file);
        return outputFile;
    }

    private File preProcessing() throws IOException {       // preparing file for transformation. Splits whole matrix of pixels on several matrices of 8x8 pixels
        height = image.getHeight();
        width = image.getWidth();
        SIZE = height * width;
        // make matrix of pixels
        Color[][] matrixData = new Color[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Color color = new Color(image.getRGB(j, i));
                matrixData[i][j] = color;
                int a = color.getBlue();
                int b = a;
            }
        }
        double[][] matrixY = new double[height][width];
        double[][] matrixCb = new double[height][width];
        double[][] matrixCr = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                RealMatrix YCbCr = toYCbCr(matrixData[i][j]);
                matrixY[i][j] = YCbCr.getEntry(0, 0);
                matrixCb[i][j] = YCbCr.getEntry(1, 0);
                matrixCr[i][j] = YCbCr.getEntry(2, 0);
            }
        }
        // сделал массивы со всеми спктрами по отдельности
        int thisHeight = height / 8;
        int thisWidth = width / 8;
        RealMatrix[][] matrix8x8ofY = new RealMatrix[thisHeight][thisWidth];
        RealMatrix[][] matrix8x8ofCb = new RealMatrix[thisHeight][thisWidth];
        RealMatrix[][] matrix8x8ofCr = new RealMatrix[thisHeight][thisWidth];
        double[][] bufferY = new double[8][8];
        double[][] bufferCb = new double[8][8];
        double[][] bufferCr = new double[8][8];
        for (int i = 0; i < thisHeight; i++) {
            for (int j = 0; j < thisWidth; j++) {
                for (int k = 0; k < 8; k++) {
                    for (int l = 0; l < 8; l++) {
                        bufferY[k][l] = matrixY[i * 8 + k][j * 8 + l];
                        bufferCb[k][l] = matrixCb[i * 8 + k][j * 8 + l];
                        bufferCr[k][l] = matrixCr[i * 8 + k][j * 8 + l];
                    }
                }
                matrix8x8ofY[i][j] = MatrixUtils.createRealMatrix(bufferY);
                matrix8x8ofCb[i][j] = MatrixUtils.createRealMatrix(bufferCb);
                matrix8x8ofCr[i][j] = MatrixUtils.createRealMatrix(bufferCr);
            }
        }
        //  make two demensional matrix of 8x8 matrices for Y, Cr and Cb matrices
        return compress(matrix8x8ofY, matrix8x8ofCb, matrix8x8ofCr);
    }

    private File compress(RealMatrix[][] Y, RealMatrix[][] Cb, RealMatrix[][] Cr) throws IOException { // DCT transformation function

//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//            }
//            System.out.println();
//        }
//        System.out.println();
        for (int k = 0; k < height / 8; k++) {
            for (int l = 0; l < width / 8; l++) {
                RealMatrix Ybuf = U.multiply(Y[k][l]);
                RealMatrix Cbbuf = U.multiply(Cb[k][l]);
                RealMatrix Crbuf = U.multiply(Cr[k][l]);
                RealMatrix Yresult = Ybuf.multiply(Utransposed);
                RealMatrix Cbresult = Cbbuf.multiply(Utransposed);
                RealMatrix Crresult = Crbuf.multiply(Utransposed);
//                if (k == 0 && l == 0) {
//                    for (int i = 0; i < 8; i++) {
//                        for (int j = 0; j < 8; j++) {
//                            System.out.print((int) Yresult.getEntry(i, j) + " ");
//                        }
//                        System.out.println();
//                    }
//                }
                int valueY;
                int valueCb;
                int valueCr;
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        valueY = (int) (Math.round(Yresult.getEntry(j, i) * 1000.0) / 1000.0 / Z.getEntry(j, i));
                        Yresult.setEntry(j, i, valueY);
                        valueCb = (int) (Math.round(Cbresult.getEntry(j, i) * 1000.0) / 1000.0 / Z.getEntry(j, i));
                        Cbresult.setEntry(j, i, valueCb);
                        valueCr = (int) (Math.round(Crresult.getEntry(j, i) * 1000.0) / 1000.0 / Z.getEntry(j, i));
                        Crresult.setEntry(j, i, valueCr);
                    }
                }
                Y[k][l] = Yresult;          // result of compression
                Cb[k][l] = Cbresult;        // result of compression
                Cr[k][l] = Crresult;        // result of compression
            }
        }
//        System.out.println();
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//            }
//            System.out.println();
//        }

        return universalRestructureToFile(Y, Cb, Cr);
    }


    public void decompress(RealMatrix[][] Y, RealMatrix[][] Cb, RealMatrix[][] Cr) throws IOException {     // make DCT transformation in reverse order for getting initial data
//        System.out.println();
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//            }
//            System.out.println();
//        }
        for (int k = 0; k < height / 8; k++) {
            for (int l = 0; l < width / 8; l++) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        Y[k][l].setEntry(i, j, Y[k][l].getEntry(i, j) * Z.getEntry(i, j));
                        Cb[k][l].setEntry(i, j, Cb[k][l].getEntry(i, j) * Z.getEntry(i, j));
                        Cr[k][l].setEntry(i, j, Cr[k][l].getEntry(i, j) * Z.getEntry(i, j));
                    }
                }
//                if (k == 0 && l == 0) {
//                    System.out.println();
//                    for (int i = 0; i < 8; i++) {
//                        for (int j = 0; j < 8; j++) {
//                            System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//                        }
//                        System.out.println();
//                    }
//                }
                RealMatrix bufferY = Utransposed.multiply(Y[k][l]);
                RealMatrix bufferCb = Utransposed.multiply(Cb[k][l]);
                RealMatrix bufferCr = Utransposed.multiply(Cr[k][l]);
                Y[k][l] = bufferY.multiply(U);
                Cb[k][l] = bufferCb.multiply(U);
                Cr[k][l] = bufferCr.multiply(U);
//                if (k == 0 && l == 0) {
//                    System.out.println();
//                    for (int i = 0; i < 8; i++) {
//                        for (int j = 0; j < 8; j++) {
//                            System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//                        }
//                        System.out.println();
//                    }
//                }
            }
//            System.out.println();
//            for (int i = 0; i < 8; i++) {
//                for (int j = 0; j < 8; j++) {
//                    System.out.print((int) Y[0][0].getEntry(i, j) + " ");
//                }
//                System.out.println();
//            }
        }
        int[][] Ysourse = new int[height][width];
        int[][] CbSourse = new int[height][width];
        int[][] CrSourse = new int[height][width];
        for (int k = 0; k < height / 8; k++) {
            for (int l = 0; l < width / 8; l++) {
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        Ysourse[k * 8 + i][l * 8 + j] = (int) Y[k][l].getEntry(i, j);
                        CbSourse[k * 8 + i][l * 8 + j] = (int) Cb[k][l].getEntry(i, j);
                        CrSourse[k * 8 + i][l * 8 + j] = (int) Cr[k][l].getEntry(i, j);

                    }
                }

            }
        }
        print(Ysourse, CbSourse, CrSourse);
    }

    private void print(int[][] Y, int[][] Cb, int[][] Cr) throws IOException {              // makes picture from data
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                double[][] ycrcb = {{Y[i][j]}, {Cb[i][j]}, {Cr[i][j]}};
                RealMatrix ycrcbM = MatrixUtils.createRealMatrix(ycrcb);
                RealMatrix buf = toRGB(ycrcbM);
                int red = (int) buf.getEntry(0, 0);
                if (red > 255) red = 255;
                else if (red < 0) red = 0;
                int green = (int) buf.getEntry(1, 0);
                if (green > 255) green = 255;
                else if (green < 0) green = 0;
                int blue = (int) buf.getEntry(2, 0);
                if (blue > 255) blue = 255;
                else if (blue < 0) blue = 0;
                Color col = new Color(red, green, blue);
                bufferedImage.setRGB(j, i, col.getRGB());
            }
        }
        File file = new File("outFiles/");
        File[] files = file.listFiles();
        int n = 10000 + files.length;
        File returnFile = new File("outFiles/" + "IMG_" + String.valueOf(n) + ".jpeg");
        FileOutputStream name = new FileOutputStream(returnFile);
        ImageIO.write(bufferedImage, "jpeg", name);

        outputFile = returnFile;
    }

    private RealMatrix toYCbCr(Color pixel) {           // converts RGB matrix into YCrCb
        double[][] transformToYCbCr = {{0.299, 0.587, 0.114}, {-0.16875, -0.33126, 0.5}, {0.5, -0.41869, -0.08131}};
        RealMatrix matrixToYCbCr = MatrixUtils.createRealMatrix(transformToYCbCr);
        //transformation matrix to YCbCr for multiplication
        double[][] transformSum = {{0}, {128}, {128}};
        RealMatrix matrixtransformSum = MatrixUtils.createRealMatrix(transformSum);
        // matrix for sum to rgb
        double[][] pixelSpecters = {{pixel.getRed()}, {pixel.getGreen()}, {pixel.getBlue()}};
        RealMatrix rgb = MatrixUtils.createRealMatrix(pixelSpecters);
        RealMatrix resmatrix = matrixToYCbCr.multiply(rgb);
        resmatrix = matrixtransformSum.add(resmatrix);
        return resmatrix;
    }

    private RealMatrix toRGB(RealMatrix pixel) {        //converts YCrCb into RGB colorspace
        double[][] transformToRGB = {{1, 0, 1.4}, {1, -0.343, -0.711}, {1, 1.765, 0}};
        RealMatrix matrixToRGB = MatrixUtils.createRealMatrix(transformToRGB);
        double[][] pixelYCbCr = {{pixel.getEntry(0, 0)}, {pixel.getEntry(1, 0) - 128}, {pixel.getEntry(2, 0) - 128}};
        RealMatrix YCrCb = MatrixUtils.createRealMatrix(pixelYCbCr);
        RealMatrix resmatrix = matrixToRGB.multiply(YCrCb);
        return resmatrix;
    }

    public File universalRestructureToFile(RealMatrix[][] Y, RealMatrix[][] Cb, RealMatrix[][] Cr) {
        // method that transform data and writes it into file in most comfortable way
        File link = new File("compressedFile.data");
        try {
            try (FileWriter writer = new FileWriter(link, false)) {
                writer.write(String.valueOf(height));
                writer.write("\n" + String.valueOf(width) + "\n");
                for (int i = 0; i < height / 8; i++) {
                    for (int j = 0; j < width / 8; j++) {
                        ArrayList<Integer> hParam = new ArrayList<>();
                        ArrayList<Integer> wParam = new ArrayList<>();
                        int rowRes = 0;
                        int colRes = 0;
                        for (int f = 0; f < 8; f++) {
                            int rowCount = 0;
                            for (int g = 0; g < 8; g++) {
                                if ((int) Y[i][j].getEntry(f, g) != 0) rowCount++;
                            }
                            if (rowCount != 0)
                                wParam.add(rowCount);
                        }
                        for (int f = 0; f < 8; f++) {
                            int colCount = 0;
                            for (int g = 0; g < 8; g++) {
                                if ((int) Y[i][j].getEntry(g, f) != 0) colCount++;
                            }
                            if (colCount != 0)
                                hParam.add(colCount);
                        }
                        for (int k = 0; k < wParam.size(); k++) {
                            rowRes += wParam.get(k);
                        }
                        for (int k = 0; k < hParam.size(); k++) {
                            colRes += hParam.get(k);
                        }
                        if (wParam.size() == 0)
                            rowRes = 0;
                        else
                            rowRes = rowRes / wParam.size();
                        if (hParam.size() == 0)
                            colRes = 0;
                        else
                            colRes = colRes / hParam.size();
                        int q;
                        if (SIZE > LIMIT) {
                            q = Math.min(rowRes, colRes) + 1;
                        } else {
                            if (wParam.size() == hParam.size()) q = hParam.size();
                            else
                                q = Math.max(hParam.size(), wParam.size());
                        }
                        if (q > 7) q = 7;
                        writer.write(String.valueOf(q) + " ");
                        for (int k = 0; k < q; k++) {
                            for (int l = 0; l < q; l++) {

                                writer.write(String.valueOf((int) Y[i][j].getEntry(k, l)) + " ");
                            }
                        }
                    }
                }
                if (SIZE > LOWLIMIT) {
                    writer.write("\n");
                    for (int i = 0; i < height / 8; i++) {
                        for (int j = 0; j < width / 8; j++) {
                            writer.write(String.valueOf((int) Cb[i][j].getEntry(0, 0)) + " ");
                            writer.write(String.valueOf((int) Cr[i][j].getEntry(0, 0)) + " ");
                        }
                    }
                } else {
                    for (int i = 0; i < height / 8; i++) {
                        for (int j = 0; j < width / 8; j++) {
                            for (int k = 0; k < 2; k++) {
                                for (int l = 0; l < 2; l++) {
                                    writer.write(String.valueOf((int) Cb[i][j].getEntry(k, l)) + " ");
                                }
                            }
                        }
                    }
                    for (int i = 0; i < height / 8; i++) {
                        for (int j = 0; j < width / 8; j++) {
                            for (int k = 0; k < 2; k++) {
                                for (int l = 0; l < 2; l++) {
                                    writer.write(String.valueOf((int) Cr[i][j].getEntry(k, l)) + " ");
                                }
                            }
                        }
                    }
                }
                writer.flush();
                writer.close();
                return link;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    public void restructureToFile(RealMatrix[][] Y, RealMatrix[][] Cb, RealMatrix[][] Cr) {     // old function, use for debugging
        try {
            try (FileWriter writer = new FileWriter("data.txt", false)) {
                writer.write(String.valueOf(height));
                writer.write("\n" + String.valueOf(width) + "\n");
                for (int i = 0; i < height / 8; i++) {
                    for (int j = 0; j < width / 8; j++) {
                        for (int k = 0; k < 5; k++) {
                            for (int l = 0; l < 5; l++) {
                                writer.write(String.valueOf((int) Y[i][j].getEntry(k, l)) + " ");
                                writer.write(String.valueOf((int) Cb[i][j].getEntry(k, l)) + " ");
                                writer.write(String.valueOf((int) Cr[i][j].getEntry(k, l)) + " ");
                            }
                        }
                    }
                }
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decomposition(File file) throws IOException {        // prepare for reading and interpretation of data from file
        Scanner scan = null;
        try {
            scan = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int y = Integer.parseInt(scan.nextLine());
        int x = Integer.parseInt(scan.nextLine());
        int size = x * y;
        height = y;
        width = x;
        universalDecompositionFromFile();
    }

    public void universalDecompositionFromFile() throws IOException {       // reads data and transforms it into matrices for future restructuring

        Scanner scan = null;
        try {
            scan = new Scanner(new File("compressedFile.data"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int y = Integer.parseInt(scan.nextLine());
        int x = Integer.parseInt(scan.nextLine());
        int size = x * y;
        RealMatrix[][] decodedMatrixY = new RealMatrix[y / 8][x / 8];
        RealMatrix[][] decodedMatrixCb = new RealMatrix[y / 8][x / 8];
        RealMatrix[][] decodedMatrixCr = new RealMatrix[y / 8][x / 8];
        double[][] zeroArray = new double[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                zeroArray[i][j] = 0;
            }
        }
        for (int i = 0; i < y / 8; i++) {
            for (int j = 0; j < x / 8; j++) {
                decodedMatrixY[i][j] = MatrixUtils.createRealMatrix(zeroArray);
                decodedMatrixCb[i][j] = MatrixUtils.createRealMatrix(zeroArray);
                decodedMatrixCr[i][j] = MatrixUtils.createRealMatrix(zeroArray);
                int space = scan.nextInt();
                for (int k = 0; k < space; k++) {
                    for (int l = 0; l < space; l++) {
                        decodedMatrixY[i][j].setEntry(k, l, scan.nextDouble());
                    }
                }
            }
        }

        if (size > LOWLIMIT) {
            for (int i = 0; i < y / 8; i++) {
                for (int j = 0; j < x / 8; j++) {
                    decodedMatrixCb[i][j].setEntry(0, 0, scan.nextDouble());
                    decodedMatrixCr[i][j].setEntry(0, 0, scan.nextDouble());
                }
            }
        } else {
            for (int i = 0; i < y / 8; i++) {
                for (int j = 0; j < x / 8; j++) {
                    for (int k = 0; k < 2; k++) {
                        for (int l = 0; l < 2; l++) {
                            decodedMatrixCb[i][j].setEntry(k, l, scan.nextDouble());
                        }
                    }
                }
            }
            for (int i = 0; i < height / 8; i++) {
                for (int j = 0; j < width / 8; j++) {
                    for (int k = 0; k < 2; k++) {
                        for (int l = 0; l < 2; l++) {
                            decodedMatrixCr[i][j].setEntry(k, l, scan.nextDouble());
                        }
                    }
                }
            }


        }
        decompress(decodedMatrixY, decodedMatrixCb, decodedMatrixCr);
    }

    public void decompositionFromFile() throws IOException {            //for debugging
        Scanner scan = null;
        try {
            scan = new Scanner(new File("data.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int y = Integer.parseInt(scan.nextLine());
        int x = Integer.parseInt(scan.nextLine());
        RealMatrix[][] decodedMatrixY = new RealMatrix[y / 8][x / 8];
        RealMatrix[][] decodedMatrixCb = new RealMatrix[y / 8][x / 8];
        RealMatrix[][] decodedMatrixCr = new RealMatrix[y / 8][x / 8];
        double[][] bufferY = new double[8][8];
        double[][] bufferCb = new double[8][8];
        double[][] bufferCr = new double[8][8];
        RealMatrix blockY;
        RealMatrix blockCb;
        RealMatrix blockCr;
        for (int i = 0; i < y / 8; i++) {
            for (int j = 0; j < x / 8; j++) {
                for (int k = 0; k < 5; k++) {
                    for (int l = 0; l < 5; l++) {
                        bufferY[k][l] = scan.nextDouble();
                        bufferCb[k][l] = scan.nextDouble();
                        bufferCr[k][l] = scan.nextDouble();
                    }
                }
                blockY = MatrixUtils.createRealMatrix(bufferY);
                blockCb = MatrixUtils.createRealMatrix(bufferCb);
                blockCr = MatrixUtils.createRealMatrix(bufferCr);
                decodedMatrixY[i][j] = blockY;
                decodedMatrixCb[i][j] = blockCb;
                decodedMatrixCr[i][j] = blockCr;
            }
        }
        decompress(decodedMatrixY, decodedMatrixCb, decodedMatrixCr);
    }
}
