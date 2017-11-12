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
                System.out.println(s);
                JSONObject obj = null;  //Create and setup JSON object
                org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser();
                try {
                    obj = (JSONObject) parser.parse(s);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                DB db = new DB();
                //If it is for database synchronisation
                if (obj.containsKey("database")) {
                    System.out.println("Got database request.");
                    db.makeSelection(getName(), (String) obj.get("address"));
                    if (!db.hasNext()) {
                        JSONObject send = new JSONObject();
                        String receiver = (String) obj.get("address");
                        send.put("chat", receiver);
                        send.put("address", "null");
                        send.put("format", "null");
                        send.put("message", "null");
                        server.handle(send.toJSONString(), getName());
                    } else {
                        while (db.hasNext()) {
                            JSONObject send = new JSONObject();
                            String receiver = (String) obj.get("address");
                            send.put("chat", receiver);
                            send.put("address", db.get("user"));
                            send.put("format", db.get("format"));
                            send.put("message", db.get("content"));

                            System.out.println(send.toJSONString());
                            server.handle(send.toJSONString(), getName());
                            db.next();
                        }
                    }
                    db.reset();
                } else {
                    //Or send message to user.
                    db.insert(obj.get("encoded_size"), obj.get("compressed_size"),
                            getName(), obj.get("address"), obj.get("compression"), obj.get("encoding"),
                            obj.get("format"), obj.get("message"));
                    JSONObject send = new JSONObject();
                    String receiver = (String) obj.get("address");

                    send.put("address", getName());
                    send.put("format", obj.get("format"));
                    send.put("message", obj.get("message"));
                    send.put("compression", obj.get("compression"));
                    send.put("encoding", obj.get("encoding"));

                    server.handle(send.toJSONString(), receiver);
                    server.handle(send.toJSONString(), getName());
                }
                db.close();
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