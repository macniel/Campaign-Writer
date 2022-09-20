package de.macniel.campaignwriter;

import de.macniel.campaignwriter.views.BuildingView;
import de.macniel.campaignwriter.views.SessionView;
import de.macniel.campaignwriter.views.ViewInterface;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MainController {


    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;

    private ObjectProperty<String> title;

    public ObjectProperty<String> getTitle() {
        return title;
    }

    private File currentFile;

    @FXML
    private BorderPane inset;

    @SuppressWarnings("rawtypes")
    private ViewInterface activeInterface;

    @FXML
    private Menu views;


    @FXML
    public void initialize() {

        title = new SimpleObjectProperty<>();

        supportedFileExtensions = new ArrayList<>();
        supportedFileExtensions.add(new FileChooser.ExtensionFilter("campaign file", "*.campaign"));

        ArrayList<ViewInterface> views = new ArrayList<>();
        HashMap<Toggle, Map.Entry<ViewInterface, Scene>> mapping = new HashMap<>();

        views.add(new BuildingView());
        views.add(new SessionView());

        ToggleGroup viewMode = new ToggleGroup();

        views.forEach( view -> {
            try {

                String path = view.getPathToFxmlDefinition();
                String menuItemLabel = view.getMenuItemLabel();

                FXMLLoader fxmlLoader = new FXMLLoader(view.getClass().getResource(path));
                Scene scene = new Scene(fxmlLoader.load(), 480, 240);
                ViewInterface v = fxmlLoader.getController();

                RadioMenuItem item = new RadioMenuItem();
                item.setText(menuItemLabel);
                item.setToggleGroup(viewMode);
                AbstractMap.SimpleEntry<ViewInterface, Scene> set = new AbstractMap.SimpleEntry<>(v, scene);
                mapping.put(item, set);
                this.views.getItems().add(item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        viewMode.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
           Scene editor = mapping.get(t1).getValue();
           if (editor != null) {
               activeInterface = mapping.get(t1).getKey();

               inset.setCenter(editor.getRoot());
               activeInterface.requestLoad(FileAccessLayer.getInstance().getAllNotes());
           }
        });

        Optional<Map.Entry<Toggle, Map.Entry<ViewInterface, Scene>>> first = mapping.entrySet().stream().findFirst();
        if (first.isPresent()) {
            viewMode.selectToggle(first.get().getKey());
            activeInterface = first.get().getValue().getKey();
        }
    }


    @FXML public void createNewCampaign() {
        // unsaved data check
        this.currentFile = null;
        FileAccessLayer.getInstance().newCampaign();

        activeInterface.requestLoad(FileAccessLayer.getInstance().getAllNotes());
    }

    @FXML public void openCampaign() throws IOException {
        FileChooser dialog = new FileChooser();
        dialog.setTitle("Kampagne zum Öffnen auswählen");

        dialog.getExtensionFilters().setAll(supportedFileExtensions);
        File newFile = dialog.showOpenDialog(null);
        this.currentFile = newFile;
        if (newFile != null) {
            FileAccessLayer.getInstance().loadFromFile(this.currentFile);
            if(activeInterface != null) {
                activeInterface.requestLoad(FileAccessLayer.getInstance().getAllNotes());
            }
            title.set(this.currentFile.getName());
        }
    }

    @FXML public void saveCampaign() throws IOException {
        // save current note just in case
        if (activeInterface != null) {
            activeInterface.requestSave();
        }

        if (this.currentFile == null) {
            FileChooser dialog = new FileChooser();
            dialog.setTitle("Kampagne zum Speichern auswählen");
            dialog.getExtensionFilters().setAll(supportedFileExtensions);
            this.currentFile = dialog.showSaveDialog(null);
        }
        if (this.currentFile != null) {
            FileAccessLayer.getInstance().saveToFile(this.currentFile);
            title.set(this.currentFile.getName());
        }
    }

}