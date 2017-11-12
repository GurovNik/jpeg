package FrontEnd;

import Network.ChatClient;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
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
    private HBox encodingHBOX;
    @FXML
    private HBox compressionHBOX;
    @FXML
    private Button attachment;

    private ChatClient socket;
    private Map<String, Tab> tabMap;

    private EventHandler<KeyEvent> keyHandler;
    private EventHandler<KeyEvent> sendHandler;
    private EventHandler<Event> tabCloseHandler;

    @FXML
    public void initialize() {
        keyHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    String alias = sendTo.getText();
                    if (!alias.equals("")) {
                        requestHistory(alias);
                        sendTo.clear();
                    }
                }
            }
        };

        sendHandler = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER) {
                    sendData(textBar.getText());
                    textBar.clear();
                }
            }
        };

        tabCloseHandler = new EventHandler<Event>()
        {
            @Override
            public void handle(Event e)
            {
                Tab t = (Tab)(e.getSource());
                String name = t.getText();
                tabMap.remove(name);
            }
        };

        tabMap = new HashMap<>();
        sendTo.setOnKeyReleased(keyHandler);
        textBar.setOnKeyReleased(sendHandler);

        attachment.setOnAction(
                new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(final ActionEvent e) {
                        final FileChooser fileChooser = new FileChooser();
                        final File selectedFile = fileChooser.showOpenDialog(null);
                        if (selectedFile != null) {
                            sendData(selectedFile);
                        }
                    }
                }
        );

        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
    }

    public void submitTextMessage() {
        sendData(textBar.getText());
    }

    private void sendData(Object obj) {
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

    private void requestHistory(String u_name) {

        Task<Tab> tabTask = new Task<Tab>() {
            @Override
            protected Tab call() throws Exception {
                Tab tab = new Tab(u_name);
                tab.setOnCloseRequest(tabCloseHandler);
                fillTab(u_name, tab);

                return tab;
            }
        };

        tabTask.setOnSucceeded(event -> {
            JSONObject object = createHistoryRequest(u_name);
            if (object != null) {
                try {
                    socket.write(object.toJSONString());
                } catch (IOException e) {
                    System.out.println("Wrong json");
                    e.printStackTrace();
                }
            }

            tabs.getTabs().add(tabTask.getValue());
        });

        Thread th = new Thread(tabTask);
        th.setDaemon(true);
        th.start();
    }

    private JSONObject createHistoryRequest(String alias) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", alias);
        jsonObject.put("database", 1);
        return jsonObject;
    }

    public synchronized void receiveMessage(String text) {
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

        Task<Node> task = new Task<Node>() {

            @Override
            public Node call() throws Exception {
                String format = (String) obj.get("format");
                Node n;
        //        Cell<Node> cell = new Cell<>();
        //        Node n;
        //
        //        System.out.println("Received msg :: " + obj.toJSONString());
        //
        //        //TODO :: New data types
        //        //TODO :: NOT ONLY FOR STRING SAY NAME
                switch (format)

                {
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
                return n;
            }
        };

        task.setOnSucceeded(event -> {
            ListView lv = (ListView) tab.getContent();
            lv.getItems().add(task.getValue());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private Tab selectDialogue(String alias) {
        Tab tab;
        if (!tabMap.containsKey(alias)) {
//            Task<Tab> taskTab = new Task<Tab>() {
//                @Override
//                protected Tab call() throws Exception {
//                    Tab tab = new Tab(alias);
//                    ListView lv = new ListView();
//                    tab.setContent(lv);
//                    tabMap.put(alias, tab);
//
//                    return tab;
//                }
//            };
//
//            taskTab.setOnSucceeded(event -> {
//                requestHistory(alias);
//            });
            requestHistory(alias);


//            tab = new Tab(alias);
//            fillTab(alias, tab);
//            tabs.getTabs().add(tab);
//            tabMap.put(alias, tab);
//            dialogue.add(alias);
        }

        tab = tabMap.get(alias);




        return tab;
    }

    private void fillTab(String alias, Tab tab) {
        ListView lv = new ListView();
        tab.setContent(lv);
        tabMap.put(alias, tab);
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
        jsonObject.put("format", "text");
        jsonObject.put("message", data);

        return jsonObject.toJSONString();
    }



    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
