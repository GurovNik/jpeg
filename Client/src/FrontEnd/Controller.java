package FrontEnd;

import Network.ChatClient;
import javafx.collections.ObservableList;
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

import javax.print.attribute.standard.Compression;
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
    private ToggleGroup compression;
    private ToggleGroup encoding;

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

        tabCloseHandler = new EventHandler<Event>() {
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

        compression = new ToggleGroup();
        encoding = new ToggleGroup();
        ObservableList<Node> compressionButtons = compressionHBOX.getChildren();
        for (Node n: compressionButtons) {
            ((RadioButton)(n)).setToggleGroup(compression);
        }

        ObservableList<Node> encodingnButtons = encodingHBOX.getChildren();
        for (Node n: encodingnButtons) {
            ((RadioButton)(n)).setToggleGroup(encoding);
        }

    }

    public int getHBOXindex(HBox hBox) {
        ObservableList<Node> ol = hBox.getChildren();
        for (int i = 0; i < ol.size(); i++) {
            RadioButton rb = (RadioButton) ol.get(i);
            if (rb.isSelected())
                return i;
        }
        return -1;
    }

    public void submitTextMessage() {
        sendData(textBar.getText(), "text");
    }

    private void sendData(Object obj, String format) {
        int compression =  getHBOXindex(compressionHBOX);
        int encoding =  getHBOXindex(encodingHBOX);

        File objLink = saveObject(obj);
        CompressionAlgorithm cAlg = getCompressionAlgorithm(objLink,  compression);
        File cFile = cAlg.compress();
        EncoderAlgorithm eAlg = getEncodingAlgorithm(cFile,  encoding);
        File eFile = eAlg.encode();

        String json = createJSON(eFile, format);

        try {
            socket.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File saveObject(Object obj) {
        return File(obj);
        //TODO :: Сделать это
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
            if (tab != null)
                applyMessage(tab, obj);
        } catch (ParseException pe) {
            System.err.println("Invalid json");
            pe.printStackTrace();
        }
    }

    private void applyMessage(Tab tab, JSONObject obj) {
        File link = obj;
        EncodingAlgorithm eAlg = getEncodingAlgorithm(link, (int)obj.get("encoding"));
        File eFile = eAlg.decode();
        CompressionAlgorithm cAlg = getCompressionAlgorithm(eFile, (int)obj.get("compression"));
        File cFile = cAlg.decompress();
        //TODO :: decompress data for TASK
        //TODO :: Перенести код выше в TASK

        Task<Node> task = new Task<Node>() {

            @Override
            public Node call() throws Exception {
                String format = (String) obj.get("format");
                Node n;
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
                        n = new Label("File %name has come, stored in %path");
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
        if (!tabMap.containsKey(alias)) {
          requestHistory(alias);
        }

        Tab tab = tabMap.get(alias);

        return tab;
    }

    private void fillTab(String alias, Tab tab) {
        ListView lv = new ListView();
        tab.setContent(lv);
        tabMap.put(alias, tab);
    }

    private String createJSON(File link, String format) {
        String sendTo = tabs.getSelectionModel().getSelectedItem().getText();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", sendTo);
        jsonObject.put("compression", getHBOXindex(compressionHBOX));
        jsonObject.put("encoding", getHBOXindex(encodingHBOX));
        jsonObject.put("initial_size", -1);
        jsonObject.put("compressed_size", -1);
        jsonObject.put("encoded_size", -1);
        jsonObject.put("format", format);
        jsonObject.put("message", BINARY_link);
        // TODO :: LINK INTO BASE64

        return jsonObject.toJSONString();
    }

    private CompressionAlgorithm getCompressionAlgorithm(File link, int compressionMethod) {
        switch (compressionMethod) {
            // First method
            default:
            case 0:
                return Algorithms.Compression.huffman(link);
                break;
            // Second method
            case 1:
                return Algorithms.Compression.huffman(link);
                break;
            // Third method
            case 2:
                return Algorithms.Compression.huffman(link);
                break;
            }
    }

    private EncodingAlgorithm getEncodingAlgorithm(File link, int encodingMethod) {
        switch (encodingMethod){
            // First method
            default:
            case 0:
                return Algorithms.Encoding.huffman(link);
                break;
            // Second method
            case 1:
                return Algorithms.Encoding.huffman(link);
                break;
            // Third method
            case 2:
                return Algorithms.Encoding.huffman(link);
                break;
        }
    }

    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
