package de.macniel.campaignwriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CampaignWriterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CampaignWriterApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 480, 240);


        MainController controller = fxmlLoader.getController();
        controller.setStage(stage);
        scene.getStylesheets().add(CampaignWriterApplication.class.getResource("note-editor.css").toExternalForm());
        stage.setTitle("Campaign Writer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}