package FrontEnd;

import Network.ChatClient;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Controller {
    @FXML
    private TextField textBar;
    @FXML
    private TabPane tabs;
    @FXML
    private TextField sendTo;
    @FXML
    private Label alias;
    @FXML
    private Button addTab;
    @FXML
    private HBox encodingHBOX;
    @FXML
    private HBox compressionHBOX;

    private ChatClient socket;
    private Set<String> dialogue;
    private Map<String, Tab> tabMap;

    private EventHandler<KeyEvent> keyHandler;


    @FXML
    public void initialize() {
        keyHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    String alias = sendTo.getText();
                    requestHistory(alias);
                    dialogue.add(alias);
                    sendTo.clear();
                }
            }
        };

        tabMap = new HashMap<>();
        dialogue = new HashSet<>();
        sendTo.setOnKeyReleased(keyHandler);
    }


    @FXML
    public void tabme() {
        Tab tab = new Tab("Hello");
        tabs.getTabs().add(tab);
    }

    @FXML
    public void sendData() {
        Object obj = textBar.getText();
        textBar.clear();
        String json = createJSON(obj);

        try {
            socket.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveAlias(String alias) {
        this.alias.setText(alias);
    }

    private void requestHistory(String alias) {
        Tab tab = new Tab(alias);
        fillTab(alias, tab);
        tabs.getTabs().add(tab);

        JSONObject object = createHistoryRequest();
        if (object != null) {
            try {
                socket.write(object.toJSONString());
            } catch (IOException e) {
                System.out.println("Wrong json");
                e.printStackTrace();
            }
        }
    }

    private JSONObject createHistoryRequest() {
        String address = sendTo.getText();

        if (address.equals(""))
            return null;

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", address);
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
        Node n;
//        Cell<Node> cell = new Cell<>();
//        Node n;
//
//        System.out.println("Received msg :: " + obj.toJSONString());
//
//        //TODO :: New data types
//        //TODO :: NOT ONLY FOR STRING SAY NAME
        switch (format) {
            case "text":
                String data = (String) obj.get("message");
                n = new Label(obj.get("address") + " :: " + data);
                break;
            case "jpeg":
                n = new ImageView("@../../image.jpeg");
                // Image has to be in Client/src folder
                break;
            default:
                n = new Label("No data");
                break;
        }


//        n = new ImageView("@../../image.jpeg");
        lv.getItems().add(n);
    }

    private Tab selectDialogue(String alias) {
        Tab tab;
        if (!dialogue.contains(alias)) {
            tab = new Tab(alias);
            fillTab(alias, tab);
            tabs.getTabs().add(tab);

            tabMap.put(alias, tab);
            dialogue.add(alias);
//            TODO :: наполнение таба
        } else {
            tab = findTabByAlias(alias);
        }

        return tab;
    }

    private void fillTab(String alias, Tab tab) {
        ListView lv = new ListView();
        tab.setContent(lv);
        tabMap.put(alias, tab);
    }

    private Tab findTabByAlias(String alias) {
        if (tabMap.containsKey(alias))
            return tabMap.get(alias);
        return null;
    }

    private String createJSON(Object data) {
        String sendTo = tabs.getSelectionModel().getSelectedItem().getText();
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
