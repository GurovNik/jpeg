package FrontEnd;

import Network.ChatClient;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.json.simple.JSONObject;

import java.io.IOException;

public class Controller {
    @FXML
    private ListView listView;
    @FXML
    private TextField textBar;
    private ChatClient socket = null;

    @FXML
    public void initialize() {

    }


    public void sendText() {
        String s = textBar.getText();

        try {
            socket.write(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void receiveMessage(String text) {
        ListCell<Node> listCell = new ListCell<>();
        Node l = new Label(text + " :: received");
        listCell.setItem(l);
        try {
            listView.getItems().add(l);
        } catch (java.lang.IllegalStateException exc) {
            System.out.println("Mne pohui");
        }
    }

    @FXML
    public void addText() {
        sendText();
        textBar.clear();
    }



    private String createJSON(Object data) {
        String sendTo = "ViPivaso";
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("address", sendTo);
        jsonObject.put("compression", -1);
        jsonObject.put("encoding", -1);
        jsonObject.put("initial_size", -1);
        jsonObject.put("compressed_size", -1);
        jsonObject.put("final_size", -1);
        jsonObject.put("format", -1);
        jsonObject.put("message", data);

        return jsonObject.toJSONString();
    }


    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
