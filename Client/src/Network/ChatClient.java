package Network;

import FrontEnd.Controller;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient extends Thread {
    private Socket socket = null;
    private Thread thread = null;
    private DataInputStream console = null;
    private DataOutputStream streamOut = null;
    private ChatClientThread client = null;
    private Controller frontEndController = null;

    public ChatClient(String serverName, int serverPort, Controller controller) {
        System.out.println("Establishing connection. Please wait ...");
        try {
            socket = new Socket(serverName, serverPort);
            frontEndController = controller;
            System.out.println("Connected: " + socket);
            start();
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public void throwAlias(String alias) {
        frontEndController.receiveAlias(alias);
    }

    public void run() {

    }

    public void sendFile(File file, String msg){
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
            System.out.println("CALL FOR FILE :: " + msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            Thread.sleep(510);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int count;
        byte bytes[] = new byte[8192];
        DataInputStream inputStream = null;
        try {
            inputStream = new DataInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            int allocationSize = (int)file.length();
            while((count = inputStream.read(bytes, 0, Math.min(8192, allocationSize)))>0){
                streamOut.write(bytes);
                streamOut.flush();
                System.out.println("Left :: " + allocationSize);
                allocationSize -= count;
            }
            streamOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(String msg) throws IOException {
        streamOut.writeUTF(msg);
        streamOut.flush();
        System.out.println(msg);
    }

    public void handle(String msg) throws IOException {
        JSONParser parser = new JSONParser();
        JSONObject obj= null;
        try {
            obj = (JSONObject) parser.parse(msg);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("RECEIVED :: " + obj.toJSONString());
        if(obj.containsKey("database")){
            frontEndController.receiveMessage(msg);
        }else{
            if(!((String)obj.get("format")).equals("text")){
                System.out.println("FILE RECEIVED!");
                File f = client.readFile(Integer.parseInt((String) obj.get("encoded_size")), msg);
                System.out.println("Huina");
                frontEndController.receiveMessage(msg,f);
            }else{
                frontEndController.receiveMessage(msg);
            }
        }
    }

    @Deprecated
    public void login(String login){
        try {
            streamOut.writeUTF(login);
            streamOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        console = new DataInputStream(System.in);
        try {
            streamOut = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (thread == null) {
            client = new ChatClientThread(this, socket);
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stope() {
        if (thread != null) {
            thread.stop();
            thread = null;
        }
        try {
            if (console != null) console.close();
            if (streamOut != null) streamOut.close();
            if (socket != null) socket.close();
        } catch (IOException ioe) {
            System.out.println("Error closing ...");
        }
        client.close();
        client.stop();
    }
}