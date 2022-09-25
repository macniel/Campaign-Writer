package de.macniel.campaignwriter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class CampaignWriterApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CampaignWriterApplication.class.getResource("main-view.fxml"));

        FileAccessLayer.getInstance().getGlobal("width").ifPresent(loadedWidth -> {
            stage.setWidth(Double.valueOf(loadedWidth));
        });
        FileAccessLayer.getInstance().getGlobal("height").ifPresent(loadedHeight -> {
            stage.setHeight(Double.valueOf(loadedHeight));
        });
    
        Scene scene = new Scene(fxmlLoader.load());

        
        stage.heightProperty().addListener( (observable, oldHeight, newHeight) -> {
            FileAccessLayer.getInstance().updateGlobal("height", newHeight.toString());
        });
        stage.widthProperty().addListener( (observable, oldWidth, newWidth) -> {
            FileAccessLayer.getInstance().updateGlobal("width", newWidth.toString());
        });
        stage.xProperty().addListener( (observable, oldX, newX) -> {
            FileAccessLayer.getInstance().updateGlobal("x", newX.toString());
        });
        stage.yProperty().addListener( (observable, oldY, newY) -> {
            FileAccessLayer.getInstance().updateGlobal("y", newY.toString());
        });

        FileAccessLayer.getInstance().getGlobal("x").ifPresent(x -> {
            stage.setX(Double.valueOf(x));
        });

        FileAccessLayer.getInstance().getGlobal("y").ifPresent(y -> {
            stage.setY(Double.valueOf(y));
        });

        MainController controller = fxmlLoader.getController();
        scene.getStylesheets().add(CampaignWriterApplication.class.getResource("note-editor.css").toExternalForm());

        controller.getTitle().addListener( (change, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                stage.setTitle("Campaign Writer - " + newValue);
            } else {
                stage.setTitle("Campaign Writer");
            }
        });

        FileAccessLayer.getInstance().getGlobal("lastFilePath").ifPresent(lastFilePath -> {

                controller.openCampaign(new File(lastFilePath));
            
        });


        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_512.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_256.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_128.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_32.png")));


        stage.setTitle("Campaign Writer");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}