package Algorithm;

import java.io.File;

public class Repetition3 {

    private File input;

//    public Repetition3(File input){
//        this.input = input;
//    }

    String codeString(String input){
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++){
            String repeated = "" + input.charAt(i) + input.charAt(i) + input.charAt(i);
            result.append(repeated);
        }
        return result.toString();
    }

    String decodeString(String code){
        StringBuilder result = new StringBuilder();
        int counter = 0;
        while (counter < code.length()){
            String repeated = "" + code.charAt(counter) + + code.charAt(counter + 1) + code.charAt(counter + 2);
            int zero = 0;
            int one = 0;
            for (int i = 0; i < 3; i++){
                if (repeated.charAt(i) == '0') zero++;
                else if (repeated.charAt(i) == '1') one++;
            }
            if (zero > one) result.append("0");
            else if (one > zero) result.append("1");
            counter = counter + 3;
        }
        return result.toString();
    }
}
