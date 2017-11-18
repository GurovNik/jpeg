//package FrontEnd;
//
//import Network.ChatClient;
//import Network.ChatClientThread;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.concurrent.Task;
//import javafx.event.EventHandler;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.TextInputDialog;
//import javafx.stage.Stage;
//import javafx.stage.WindowEvent;
//
//import java.util.Optional;
//
//public class Main extends Application {
//    static Controller myController;
//    static ChatClient chatClient;
//    static ChatClientThread chatClientThread;
//
//    @Override
//    public void start(Stage primaryStage) throws Exception{
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
//        Parent root = loader.load();
//        myController = (Controller)loader.getController();
//
//        primaryStage.setTitle("Messenger v0.01");
//        Scene scene = new Scene(root);
//        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
//        primaryStage.setScene(scene);
//        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
//            @Override
//            public void handle(WindowEvent event) {
//                Platform.exit();
//                System.exit(0);
//            }
//        });
//
//        primaryStage.show();
//
//        chatClient = new ChatClient("localhost", 3388, myController);
//        myController.setSocket(chatClient);
//
//        Platform.runLater(chatClient);
//        startDialogue();
//    }
//
//
//    public static void main(String[] args) {
//        launch(args);
//    }
//
//    private void startDialogue() {
//        TextInputDialog dialog = new TextInputDialog("alias");
//        dialog.setTitle("Alias");
//        dialog.setHeaderText("Insert your alias");
//        dialog.setContentText("Please enter your alias:");
//
//        Optional<String> result = dialog.showAndWait();
//
//        if (result.isPresent()) {
//            String answer = result.get();
//            chatClient.login(answer);
//            chatClient.throwAlias(answer);
//        }
//        else
//            startDialogue();
//    }
//}
