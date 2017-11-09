package Network;

import FrontEnd.Controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ChatClient implements Runnable {
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
//            start();
        } catch (UnknownHostException uhe) {
            System.out.println("Host unknown: " + uhe.getMessage());
        } catch (IOException ioe) {
            System.out.println("Unexpected exception: " + ioe.getMessage());
        }
    }

    public static void main(String args[]) {
        ChatClient client = null;
        // Huevo
        client = new ChatClient("localhost", 3388, null);
    }

//    public void run() {
//        while (thread != null) {
//
//        }
//    }

    public void write(String msg) throws IOException {
        streamOut.writeUTF(msg);
        streamOut.flush();
        System.out.println(msg);
    }

    public void handle(String msg) throws IOException {
        System.out.println(msg);
        frontEndController.receiveMessage(msg);
    }

//    public void start() throws IOException {
//        console = new DataInputStream(System.in);
//        streamOut = new DataOutputStream(socket.getOutputStream());
//        if (thread == null) {
//            client = new ChatClientThread(this, socket);
//            thread = new Thread(this);
//            thread.start();
//        }
//    }

    @Override
    public void run() {
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
//        while (thread != null) {
//
//        }
    }

    public void stop() {
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