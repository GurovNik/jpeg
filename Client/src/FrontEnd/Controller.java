package FrontEnd;

import Network.ChatClient;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller {
    @FXML
    private VBox vboxMessages;
    @FXML
    private ListView listView;
    @FXML
    private TextField textBar;
    private ChatClient socket = null;

    @FXML
    public void initialize() {
        VBox.setVgrow(vboxMessages, Priority.ALWAYS);
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


    public void setSocket(ChatClient socket) {
        this.socket = socket;
    }
}
