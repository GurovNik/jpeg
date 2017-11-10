import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatServerThread extends Thread {
    private ChatServer server = null;
    private Socket socket = null;
    private int ID = -1;
    private DataInputStream streamIn = null;
    private DataOutputStream streamOut = null;

    protected ChatServerThread(ChatServer _server, Socket _socket) {
        super();
        server = _server;
        socket = _socket;
        ID = socket.getPort();
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch (IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            stop();
        }
    }

    public int getID() {
        return ID;
    }

    public void run() {
        System.out.println("Network.Server Thread " + ID + " running.");
        while (true) {
            try {
                String s = streamIn.readUTF();

                JSONObject obj = null;
                JSONObject send = new JSONObject();
                org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                try {
                    obj = (JSONObject) parser.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String receiver = (String) obj.get("address");
                System.out.println(receiver);
                send.put("address", getName());
                send.put("format", -1);
                send.put("message", obj.get("message"));
                server.handle(getName(), send.toJSONString(), receiver);
            } catch (IOException ioe) {
                System.out.println(getName() + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }

    public void open() throws IOException {
        streamIn = new DataInputStream(new
                BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new
                BufferedOutputStream(socket.getOutputStream()));
        System.out.println("Waiting for a name...");
        setName(streamIn.readUTF());
        System.out.println(getName() + " has logged in.");
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
        if (streamIn != null) streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}