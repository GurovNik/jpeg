package FrontEnd.sample;

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

public class Controller {
    @FXML
    private VBox vboxMessages;
    @FXML
    private ListView listView;
    Integer clientsocket;

    int i = 1;

    @FXML
    public void initialize() {
        clientsocket = new Integer(5);

        VBox.setVgrow(vboxMessages, Priority.ALWAYS);
    }
    @FXML
    public void addText() {
        ListCell<Node> listCell = new ListCell<>();
        Node l = new Label("Kekoza" + Integer.toString(i++));
        listCell.setItem(l);
        listCell.setOnMouseClicked(new EventHandler<Event>() {

            @Override
            public void handle(Event me) {
                System.out.println(((Control)me.getSource()).getId());
            }
        });
        if (i % 2 == 0) {
            listCell.setId("chosen");
        }

//        VBox.setMargin(l, new Insets(10, 10, 10, 10));
//        vboxMessages.getChildren().add(l);
//        ListView.
        listView.getItems().add(l);
    }
}
