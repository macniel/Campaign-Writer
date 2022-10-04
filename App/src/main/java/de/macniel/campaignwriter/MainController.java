package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.ModulePlugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController {


    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;

    private ObjectProperty<String> title;
    @FXML
    private Menu dataProviders;
    private Stage parentWnd;

    public ObjectProperty<String> getTitle() {
        return title;
    }

    private File currentFile;

    @FXML
    private BorderPane inset;

    private ModulePlugin activeInterface;

    @FXML
    private Menu views;

    private ArrayList<ModulePlugin> viewerPlugins = new ArrayList<>();

    private ToggleGroup viewMode;

    private HashMap<Toggle, Map.Entry<ModulePlugin, Scene>>  mapping;

    private ResourceBundle i18n;


    @FXML
    public void initialize() {
        i18n = ResourceBundle.getBundle("i18n.base");

        title = new SimpleObjectProperty<>();

        supportedFileExtensions = new ArrayList<>();
        supportedFileExtensions.add(new FileChooser.ExtensionFilter(
            i18n.getString("fileFormatName")
            , "*.campaign"));

        mapping = new HashMap<>();

        viewMode = new ToggleGroup();

        Registry.getInstance().getAllModules().forEach(view -> {
            try {

                String path = view.getPathToFxmlDefinition();
                String menuItemLabel = view.getMenuItemLabel();

                try {
                    String basePath = (String) view.getClass().getMethod("getLocalizationBase").invoke(null);
                    System.out.println(path);
                    FXMLLoader fxmlLoader = new FXMLLoader(view.getClass().getResource(path), ResourceBundle.getBundle(basePath));
                    Scene scene = new Scene(fxmlLoader.load(), 480, 240);

                    ModulePlugin v = fxmlLoader.getController();

                    RadioMenuItem item = new RadioMenuItem();

                    item.setText(menuItemLabel);
                    item.setToggleGroup(viewMode);

                    AbstractMap.SimpleEntry<ModulePlugin, Scene> set = new AbstractMap.SimpleEntry<>(v, scene);
                    mapping.put(item, set);
                    this.views.getItems().add(item);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }


            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        viewMode.selectedToggleProperty().addListener((observableValue, toggle, t1) -> {
           switchViewer(t1);
        });
        // viewMode.selectToggle(viewMode.getToggles().get(0));
        Registry.getInstance().getAllDataProviders().forEach(provider -> {
            MenuItem tmp = new MenuItem ();
            provider.setOnFinishedTask(param -> {
                System.out.println("returned from task with state");
                return true;
            });
            provider.setOnGenerateNote(note -> {
                FileAccessLayer.getInstance().getAllNotes().add(note);
                try {
                    reloadCampaign();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            });
            tmp.setText(provider.menuItemLabel());
            // FIXME: Only provide a copy!
            tmp.setOnAction(event -> provider.startTask(FileAccessLayer.getInstance().getFile(), parentWnd, FileAccessLayer.getInstance()));
            this.dataProviders.getItems().add(tmp);
        });
        openLastViewer();
    }

    void switchViewer(Toggle present) {
        Scene editor = mapping.get(present).getValue();
        if (editor != null) {
            activeInterface = mapping.get(present).getKey();
            FileAccessLayer.getInstance().updateSetting("lastModule", activeInterface.getMenuItemLabel());

            inset.setCenter(editor.getRoot());
            activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
            activeInterface.requestLoadNote(param -> {

                FileAccessLayer.getInstance().findByReference(param).ifPresent(foundNote -> {
                    System.out.println("request to open " + foundNote + " as type " + foundNote.getType());
                });
                return true;
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
        dialog.setTitle(i18n.getString("openFileDialogTitle"));

        dialog.getExtensionFilters().setAll(supportedFileExtensions);
        File newFile = dialog.showOpenDialog(null);
        this.currentFile = newFile;
        if (newFile != null) {
            FileAccessLayer.getInstance().loadFromFile(this.currentFile);
            if(activeInterface != null) {
                activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
            }
            title.set(this.currentFile.getName());

            openLastViewer();        }
    }

    public void reloadCampaign() throws IOException {
        // save current note just in case
        if (activeInterface != null) {
            activeInterface.requestSave();
            FileAccessLayer.getInstance().saveToFile();
            FileAccessLayer.getInstance().loadFromFile(this.currentFile);
            activeInterface.requestLoad(FileAccessLayer.getInstance().getFile());
        }
    }

    @FXML public void saveCampaign() throws IOException {
        // save current note just in case
        if (activeInterface != null) {
            activeInterface.requestSave();
        }

        if (this.currentFile == null) {
            FileChooser dialog = new FileChooser();
            dialog.setTitle(i18n.getString("saveFileDialogTitle"));
            dialog.getExtensionFilters().setAll(supportedFileExtensions);
            this.currentFile = dialog.showSaveDialog(null);

        }
        if (this.currentFile != null) {
            FileAccessLayer.getInstance().saveToFile(this.currentFile);
            title.set(this.currentFile.getName());
        }
    }

    public void setStage(Stage parentWnd) {
        this.parentWnd = parentWnd;
    }

}