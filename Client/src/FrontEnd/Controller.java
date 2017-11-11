package FrontEnd;

import Network.ChatClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.*;

public class Controller {
    @FXML
    private TextField textBar;
    @FXML
    private TabPane tabs;
    @FXML
    private TextField sendTo;

    private ChatClient socket = null;
    private Set<String> dialogue;
    private Map<String, Tab> tabMap;

    private EventHandler<KeyEvent> keyHandler;


    @FXML
    public void initialize() {
        keyHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    requestHistory();
                }
            }
        };

        tabMap = new HashMap<>();
        dialogue = new HashSet<>();
        //TODO :: шото тут хотел
        sendTo.setOnKeyReleased(keyHandler);
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

    private void requestHistory() {
        JSONObject object = createHistoryRequest();

        try {
            socket.handle(object.toJSONString());
        } catch (IOException e) {
            System.out.println("Wrong json");
            e.printStackTrace();
        }
    }

    private JSONObject createHistoryRequest() {
        String sendTo = tabs.getSelectionModel().getSelectedItem().getText();
        System.out.printf("Send to is equal to :: %s\n", sendTo);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", sendTo);
        jsonObject.put("database", 1);
        return jsonObject;
    }

    public void receiveMessage(String text) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject obj = (JSONObject) parser.parse(text);
            String room = (String) obj.get("chat");
            Tab tab = selectDialogue(room);
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

        //TODO :: New data types
        //TODO :: NOT ONLY FOR STRING SAY NAME
        switch (format) {
            case "text":
                String data = (String) obj.get("message");
                n = new Label(obj.get("address") + " :: " + data);
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
            tab = new Tab();
            tab.setText(alias);
            ListView lv = new ListView();
            tab.setContent(lv);
            tabs.getTabs().add(tab);

            tabMap.put(alias, tab);
            dialogue.add(alias);
//            TODO :: наполнение таба
        } else {
            tab = findTabByAlias(alias);
        }

        return tab;
    }

    private Tab findTabByAlias(String alias) {
        //        TODO :: ХешМэп tab -> alias
        if (tabMap.containsKey(alias))
            return tabMap.get(alias);
        return null;
    }

    private String createJSON(Object data) {
        String sendTo = tabs.getSelectionModel().getSelectedItem().getText();
        System.out.printf("Send to is equal to :: %s\n", sendTo);
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
