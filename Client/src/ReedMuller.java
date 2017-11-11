/**
 * Created by Arsee on 08.11.2017.
 */
public class ReedMuller {
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


    public int [] encode (int ar[]){
        int out [] = new int [(int)Math.ceil(ar.length/1.0/encodeMx.length)*encodeMx[0].length];
        int ind=0;

        for (int i=0;i<(Math.ceil(ar.length/encodeMx.length));i+=1){
            for (int j=0;j<encodeMx[0].length;j++){
                int sum=0;
                for (int f=0;f<encodeMx.length;f++){
                    sum=sum^(ar[encodeMx.length*i+f]*encodeMx[f][j]);
                }
                out[ind]=sum;
                ind++;
            }
        }

        return  out;
    }

    public int[] decode (int ar[]){
        int out [] = new int [ar.length/encodeMx[0].length*encodeMx.length];
        int ind=0;
        for (int i=0;i<ar.length/(encodeMx[0].length);i++){
            int value[] = new int [encodeMx[0].length];
                for (int f=0;f<MX.length;f++){
                    int sum=0;
                    for (int j=0;j<MX[0].length;j++){
                        sum+=MX[f][j]*(2*ar[encodeMx[0].length*i+j]-1);
                    }
                    value[f]=sum;
                }
                int max=0;
                int val=0;
                int in=0;
                for (int j=0;j<value.length;j++){
                    if (max<Math.abs(value[j])){
                        max=Math.abs(value[j]);
                        val=value[j];
                        in=j;
                    }
                }

                    int x1 = ((1+MX[in][8])/2)^((1+MX[in][0])/2);
                    int x2 = ((1+MX[in][4])/2)^((1+MX[in][0])/2);
                    int x3 = ((1+MX[in][2])/2)^((1+MX[in][0])/2);
                    int x4 = ((1+MX[in][1])/2)^((1+MX[in][0])/2);
                    int x0 =ar[i*16];
                    if (val>0){
                        x0=1;
                    }else{
                        x0=0;
                    }
                    out[ind] = x0;
                    out[ind+1]=x1;
                    out[ind+2]=x2;
                    out[ind+3]=x3;
                    out[ind+4]=x4;
                    ind+=5;


        }
        return  out;
    }
}
