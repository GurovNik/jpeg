package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    static Controller myController;

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("sample.fxml"));
        Parent root = loader.load();
        myController = (Controller)loader.getController();

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root));


        primaryStage.show();
        TempThread myThread = new TempThread(myController);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
