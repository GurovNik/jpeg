package FrontEnd;

import Network.ChatClient;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class Controller {
    @FXML
    private TextField textBar;
    @FXML
    private TabPane tabs;
    @FXML
    private TextField sendTo;

    private ChatClient socket = null;
    private Set<String> dialogue;


    @FXML
    public void initialize() {
        dialogue = new TreeSet<>();
    }

    @FXML
    public void sendData() {
        Object obj = textBar.getText();
        textBar.clear();
        String json = createJSON(obj);
        System.out.println(json);

        try {
            socket.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(String text) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(text);
            String sender = (String) obj.get("sender");
            Tab tab = selectDialogue(sender);
            if (tab != null) {
                applyMessage(tab, obj);
            }// TODO :: выебон сета!

        } catch (ParseException pe) {
            System.err.println("Invalid json");
            pe.printStackTrace();
        }
    }

    private void applyMessage(Tab tab, JSONObject obj) {
        ListView lv = (ListView) tab.getContent();
        String format = (String) obj.get("format");
        ListCell<Node> cell = new ListCell<>();
        Node n;

        switch (format) {
            case "text":
                String data = (String) obj.get("message");
                n = new Label(data);
                break;
            default:
                n = new Label("No data");
                break;
        }

        cell.setItem(n);
        lv.getItems().add(cell);
    }

    private Tab selectDialogue(String alias) {
        Tab tab;
        if (!dialogue.contains(alias)) {
            dialogue.add(alias);
            tab = new Tab();
            tab.setText(alias);
            ListView lv = new ListView();
            tab.setContent(lv);
//            TODO :: наполнение таба
            tabs.getTabs().add(tab);
        } else {
            tab = findTabByAlias(alias);
        }

        return tab;
    }

    private Tab findTabByAlias(String alias) {
        //        TODO :: ХешМэп tab -> alias
        for (Tab tab: tabs.getTabs()) {
            if (tab.getText().equals(alias))
                return tab;
        }
        return null;
    }

    private String createJSON(Object data) {
        String sendTo = "ViPivaso";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", sendTo);
        jsonObject.put("compression", -1);
        jsonObject.put("encoding", -1);
        jsonObject.put("initial_size", -1);
        jsonObject.put("compressed_size", -1);
        jsonObject.put("encoded_size", -1);
        jsonObject.put("format", -1);
        jsonObject.put("message", data);

        return jsonObject.toJSONString();
    }


    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
