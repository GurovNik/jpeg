package FrontEnd;

import Algorithm.CompressionAlgorithm;
import Algorithm.EncodeAlgorithm;
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
import java.io.*;
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

    private int temporaryIndex = 0;

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
                    JSONObject json = new JSONObject();
                    json.put("message", textBar.getText());
                    
                    sendData(saveObject(json), "text");
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
                        String format = getFilenameExtension(selectedFile);
                        if (selectedFile != null) {
                            sendData(selectedFile, format);
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

    private String getFilenameExtension(File link) {
        String name = link.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void submitTextMessage() {
        try {
            FileWriter fw = new FileWriter("temp_text.data");
            fw.write(textBar.getText());
            fw.close();

            File link = new File("temp_text.data");
            sendData(link, "text");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData(File link, String format) {
        int compression =  getHBOXindex(compressionHBOX);
        int encoding =  getHBOXindex(encodingHBOX);

//        CompressionAlgorithm cAlg = getCompressionAlgorithm(compression);
//        File cFile = cAlg.compress(link);
//        EncodeAlgorithm eAlg = getEncodingAlgorithm(encoding);
//        File eFile = eAlg.encode(cFile);

        EncodeAlgorithm eAlg = getEncodingAlgorithm(encoding);
        File eFile = eAlg.encode(link);

        String json = createJSON(eFile, format, link.length(), -1);

        try {
            socket.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File saveObject(JSONObject json) {
        String s = (String) json.get("message");
        String fileName = "temporary" + Integer.toString(temporaryIndex);
        try {
            FileWriter fw = new FileWriter(fileName);
            fw.write(s);
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        File link = new File(fileName);
        return link;
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

        //TODO :: decompress data for TASK
        //TODO :: Перенести код выше в TASK
        System.out.println("Я заебался :: " + obj.toJSONString());

        Task<Node> task = new Task<Node>() {

            @Override
            public Node call() throws Exception {
                File link = saveObject(obj);
                EncodeAlgorithm eAlg = getEncodingAlgorithm(Integer.parseInt((String)obj.get("encoding")));
                File eFile = eAlg.decode(link);
//                CompressionAlgorithm cAlg = getCompressionAlgorithm((int)obj.get("compression"));
//                File cFile = cAlg.decompress(link);
//                File cFile = cAlg.decompress(eFile);
                File cFile = eFile;

                String format = (String) obj.get("format");

                Node n;
        //        //TODO :: New data types
        //        //TODO :: NOT ONLY FOR STRING SAY NAME
                switch (format)
                {
                    case "text":
                        String data = readFile(cFile);
                        // Добавить декомпрессию
                        System.out.println("Data from file :: " + data);
                        n = new Label(obj.get("address") + " :: " + data);
                        break;
//                    case "jpeg":
//                        n = new ImageView("@../../image.jpeg");
//                        // Image has to be in Client/src folder
//                        break;
                    default:
                        String fileMessage = String.format("File [%s] :: stored in [%s]", cFile.getName(), cFile.getPath());
                        n = new Label(fileMessage);
                        break;
                }


//        n = new ImageView("@../../image.jpeg");
                System.out.println("Около окончания");
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

    private String createJSON(File link, String format, long initial_size, long compressed_size) {
        String sendTo = tabs.getSelectionModel().getSelectedItem().getText();

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", sendTo);
        jsonObject.put("compression", Long.toString(getHBOXindex(compressionHBOX)));
        jsonObject.put("encoding", Long.toString(getHBOXindex(encodingHBOX)));
        jsonObject.put("initial_size", Long.toString(initial_size));
        jsonObject.put("compressed_size", "-1");
        jsonObject.put("encoded_size", Long.toString(link.length()));
        jsonObject.put("format", format);

        String msg = readFile(link);

        jsonObject.put("message", msg);
        // TODO :: LINK INTO BASE64

        return jsonObject.toJSONString();
    }

    private String readFile(File link) {
        BufferedInputStream bin = null;
        try {
            bin = new BufferedInputStream(new FileInputStream(link));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        int data;
        StringBuilder factory = new StringBuilder();
        try {
            while((data = bin.read())!=-1){
                factory.append((char)data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return factory.toString();
    }

    private CompressionAlgorithm getCompressionAlgorithm(int compressionMethod) {
        switch (compressionMethod) {
            // First method
            default:
            case 0:
                return new Algorithm.Huffman("asdf");
            // Second method
//            case 1:
//                return new Algorithm.Compression.huffman(link);
//                break;
//            // Third method
//            case 2:
//                return new Algorithm.Compression.huffman(link);
//                break;
            }
    }

    private EncodeAlgorithm getEncodingAlgorithm(int encodingMethod) {
        switch (encodingMethod){
            // First method
            default:
            case 0:
                return new Algorithm.Repetition(3);
            // Second method
//            case 1:
//                return new Algorithm.Encoding.huffman(link);
//                break;
//            // Third method
//            case 2:
//                return new Algorithm.Encode.huffman(link);
//                break;
        }
    }

    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
