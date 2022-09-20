package de.macniel.campaignwriter;

import de.macniel.campaignwriter.editors.*;
import de.macniel.campaignwriter.views.BuildingView;
import de.macniel.campaignwriter.views.SessionView;
import de.macniel.campaignwriter.views.ViewInterface;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {


    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;

    private ArrayList<EditorPlugin> plugins;

    private File currentFile;

    @FXML
    private RadioMenuItem viewBuilding;
    @FXML
    private RadioMenuItem viewSession;
    @FXML
    private RadioMenuItem viewEncounter;

    @FXML
    private BorderPane inset;

    private Stage stage;

    private ViewInterface activeInterface;

    @FXML
    private Menu views;


    @FXML
    public void initialize() throws IOException {
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
               activeInterface.requestLoad(Note.getAll());
           }
        });
        Map.Entry<Toggle, Map.Entry<ViewInterface, Scene>> first = mapping.entrySet().stream().findFirst().get();
        viewMode.selectToggle(first.getKey());
        // activeInterface = first.getValue().getKey();
    }


    @FXML public void createNewCampaign() throws IOException {
        // unsaved data check
        this.currentFile = null;

        Note.removeAll();
        activeInterface.requestLoad(Note.getAll());
    }

    @FXML public void openCampaign() throws IOException {
        FileChooser dialog = new FileChooser();
        dialog.setTitle("Kampagne zum Öffnen auswählen");

        dialog.getExtensionFilters().setAll(supportedFileExtensions);
        File newFile = dialog.showOpenDialog(stage);
        this.currentFile = newFile;
        if (newFile != null) {
            FileAccessLayer.loadFromFile(this.currentFile);
            if(activeInterface != null) {
                activeInterface.requestLoad(Note.getAll());
            }
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
            this.currentFile = dialog.showSaveDialog(stage);
        }
        if (this.currentFile != null) {
            FileAccessLayer.saveToFile(this.currentFile, Note.getAll());
        }
    }

}