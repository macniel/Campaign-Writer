package de.macniel.campaignwriter;

import com.google.gson.GsonBuilder;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.Registrable;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.editors.ActorEditor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.Set;

public class CampaignWriterApplication extends Application {

    private void registerModules(String path) {

        Registry registry = Registry.getInstance();

        Reflections reflections = new Reflections(path);
        Set<Class<? extends Registrable>> allClasses =
                reflections.getSubTypesOf(Registrable.class);

        System.out.println("Found " + (allClasses.size()-1) + " registrable classes in path '" + path + "'");

        for ( Class<? extends Registrable> c : allClasses) {
            if (Modifier.isAbstract(c.getModifiers())) {
                continue;
            }
            try {
                System.out.print("registering " + c.getSimpleName());
                Object actualObject = c.getConstructor().newInstance();

                Method registerMethod = c.getMethod("register", RegistryInterface.class);
                registerMethod.invoke(actualObject, registry);
                System.out.println(" ... success");

            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.println(" ... failure");
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {

        registerModules("de.macniel.campaignwriter.editors");
        registerModules("de.macniel.campaignwriter.modules");
        registerModules("de.macniel.campaignwriter.providers");



        FXMLLoader fxmlLoader = new FXMLLoader(CampaignWriterApplication.class.getResource("main-view.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("i18n.base"));

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

        controller.setStage(stage);

        FileAccessLayer.getInstance().getGlobal("lastFilePath").ifPresent(lastFilePath -> {

                controller.openCampaign(new File(lastFilePath));
            
        });

        System.out.println(FileAccessLayer.getInstance().getTemplates().size() + " actor templates loaded");


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