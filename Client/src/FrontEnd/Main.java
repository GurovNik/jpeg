package FrontEnd;

import Network.ChatClient;
import Network.ChatClientThread;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Controller myController;
    static ChatClient chatClient;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        myController = (Controller)loader.getController();

        primaryStage.setTitle("Messenger v0.01");
        primaryStage.setScene(new Scene(root));


        primaryStage.show();
        chatClient = new ChatClient("localhost", 3388, myController);
        myController.setSocket(chatClient);
        chatClient.run();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
