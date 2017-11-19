package Network;

import java.io.*;
import java.net.Socket;

public class ChatClientThread extends Thread {
    private Socket socket = null;
    private ChatClient client = null;
    private DataInputStream streamIn = null;
    private boolean work = true;

    public ChatClientThread(ChatClient _client, Socket _socket) {
        client = _client;
        socket = _socket;
        open();
        start();
    }

    public void open() {
        try {
            streamIn = new DataInputStream(socket.getInputStream());
        } catch (IOException ioe) {
            System.out.println("Error getting input stream: " + ioe);
            work = false;
        }
    }

    public File readFile(){
        File output = new File("receivedFile");
        BufferedOutputStream streamOut = null;
        try {
            streamOut = new BufferedOutputStream(new FileOutputStream(output));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int count;
        byte bytes[] = new byte[8192];
        try {
            while((count = streamIn.read(bytes))>0){
                streamOut.write(bytes);
                streamOut.flush();
            }
            streamOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    public void close() {
        try {
            if (streamIn != null) streamIn.close();
        } catch (IOException ioe) {
            System.out.println("Error closing input stream: " + ioe);
        }
    }

    public void run() throws java.lang.IllegalStateException {
        while (work) {
            try {
                client.handle(streamIn.readUTF());
            } catch (IOException ioe) {
                System.out.println("Listening error: " + ioe.getMessage());
                client.stope();
            }
        }
    }
}