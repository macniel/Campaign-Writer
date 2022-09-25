package de.macniel.campaignwriter;

import de.macniel.campaignwriter.views.BuildingView;
import de.macniel.campaignwriter.views.EncounterView;
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
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class MainController {


    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;

    private ObjectProperty<String> title;

    public ObjectProperty<String> getTitle() {
        return title;
    }

    private File currentFile;

    @FXML
    private BorderPane inset;

    private ViewInterface activeInterface;

    @FXML
    private Menu views;

    private ArrayList<ViewInterface> viewerPlugins = new ArrayList<>();

    private ToggleGroup viewMode;

    private HashMap<Toggle, Map.Entry<ViewInterface, Scene>>  mapping;


    @FXML
    public void initialize() {

        title = new SimpleObjectProperty<>();

        supportedFileExtensions = new ArrayList<>();
        supportedFileExtensions.add(new FileChooser.ExtensionFilter("campaign file", "*.campaign"));

        mapping = new HashMap<>();

        ViewInterface bv = new BuildingView();
        AtomicReference<RadioMenuItem> br = new AtomicReference<>();

        viewerPlugins.add(bv);
        viewerPlugins.add(new SessionView());
        viewerPlugins.add(new EncounterView());

        viewMode = new ToggleGroup();

        viewerPlugins.forEach( view -> {
            try {

                String path = view.getPathToFxmlDefinition();
                String menuItemLabel = view.getMenuItemLabel();

                FXMLLoader fxmlLoader = new FXMLLoader(view.getClass().getResource(path));
                Scene scene = new Scene(fxmlLoader.load(), 480, 240);
                
                ViewInterface v = fxmlLoader.getController();

                RadioMenuItem item = new RadioMenuItem();

                item.setText(menuItemLabel);
                item.setToggleGroup(viewMode);
                if (view == bv) {
                    br.set(item);
                }
                AbstractMap.SimpleEntry<ViewInterface, Scene> set = new AbstractMap.SimpleEntry<>(v, scene);
                mapping.put(item, set);
                this.views.getItems().add(item);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        viewMode.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
           switchViewer(t1);
        });
        viewMode.selectToggle(viewMode.getToggles().get(0));
    }

    void switchViewer(Toggle present) {
        Scene editor = mapping.get(present).getValue();
        System.out.println("Toggled to " + mapping.get(present).getKey().getMenuItemLabel());
        if (editor != null) {
            activeInterface = mapping.get(present).getKey();
            FileAccessLayer.getInstance().updateSetting("lastModule", activeInterface.getMenuItemLabel());

            inset.setCenter(editor.getRoot());
            activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
            activeInterface.requestNote(new Callback<UUID,Note>() {

             @Override
             public Note call(UUID param) {
                 Optional<Note> foundNote = FileAccessLayer.getInstance().findByReference(param);
                 if (foundNote.isPresent()) {
                     return foundNote.get();
                 } else {
                     return null;
                 }
             }
              
            });
        }
    }


    @FXML public void createNewCampaign() {
        // unsaved data check
        this.currentFile = null;
        FileAccessLayer.getInstance().newCampaign();

        activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
    }

    private void openLastViewer() {
        String campaignSettingLastViewer = FileAccessLayer.getInstance().getSetting("lastModule");
        System.out.println("loading campaign with last module " + campaignSettingLastViewer);
        System.out.println("Module should load with " +FileAccessLayer.getInstance().getSetting("lastNote"));
        if (campaignSettingLastViewer != null) {
            viewMode.getToggles().stream().filter(t -> ((RadioMenuItem) t).getText().equals(campaignSettingLastViewer)).findFirst().ifPresent(toggle -> {
                switchViewer(toggle);
                viewMode.selectToggle(toggle);
        });
        } else {
            viewMode.selectToggle(viewMode.getToggles().get(0));
        }
    }

    public void openCampaign(File newFile) {
        this.currentFile = newFile;
        if (newFile != null && newFile.exists()) {
            try {
                FileAccessLayer.getInstance().loadFromFile(this.currentFile);
            
            if(activeInterface != null) {
                activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
            }
            title.set(this.currentFile.getName());
        
            openLastViewer();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        }
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
                activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
            }
            title.set(this.currentFile.getName());

            openLastViewer();
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