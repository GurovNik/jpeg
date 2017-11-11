import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;
import java.sql.SQLException;
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
                String s = streamIn.readUTF();  //read data
                JSONObject obj = null;  //Create and setup JSON object
                org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                try {
                    obj = (JSONObject) parser.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                //If it is for database synchronisation
                if (obj.containsKey("database")) {
                    System.out.println("Got database request.");
                    DB db = new DB();
                    try {
                        db.makeSelection(getName(), (String) obj.get("address"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    while (db.hasNext()) {
                        JSONObject send = new JSONObject();
                        String receiver = (String) obj.get("address");
                        try {
                            db.next();
                            send.put("chat", receiver);
                            send.put("address", db.get("string", "user"));
                            send.put("format", db.get("string", "format"));
                            send.put("message", db.get("string", "content"));
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        server.handle(send.toJSONString(), getName());
                    }
                } else {
                    //Or send message to user.
                    DB db = new DB();
                    db.insert((double) obj.get("encoded_size"), (double) obj.get("compressed_size"), getName(),
                            (String) obj.get("address"), (byte) obj.get("compression"), (byte) obj.get("encoding"),
                            (String) obj.get("format"), (String) obj.get("message"));
                    JSONObject send = new JSONObject();
                    String receiver = (String) obj.get("address");
                    System.out.println(receiver);


                    send.put("address", getName());
                    send.put("format", -1);
                    send.put("message", obj.get("message"));
                    send.put("compression", obj.get("compression"));
                    send.put("encoding", obj.get("encoding"));

                    server.handle(send.toJSONString(), receiver);
                }
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