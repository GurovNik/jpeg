package Network;

import FrontEnd.Controller;
import org.json.simple.JSONObject;
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
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int count;
        byte bytes[] = new byte[8192];
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            while((count = inputStream.read(bytes))>0){
                streamOut.write(bytes);
                streamOut.flush();
            }
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
        if(!msg.contains("text")){
            File f = client.readFile();
            //TODO: Call method for receiving a file
            // frontEndController.receiveFile(File);
        }
        frontEndController.receiveMessage(msg);
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