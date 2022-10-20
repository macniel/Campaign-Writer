package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.ModulePlugin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

public class MainController {

    public MenuItem openSettingsMenuItem;
    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;
    private ObjectProperty<String> title;
    @FXML
    private Menu dataProviders;
    private Stage parentWnd;
    private File currentFile;
    @FXML
    private BorderPane inset;
    private ModulePlugin activeInterface;
    @FXML
    private Menu views;
    private ToggleGroup viewMode;
    private HashMap<Toggle, Map.Entry<ModulePlugin, Scene>> mapping;
    private ResourceBundle i18n;

    public ObjectProperty<String> getTitle() {
        return title;
    }

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
            MenuItem tmp = new MenuItem();
            provider.setOnFinishedTask(param -> {
                System.out.println("returned from task with state");
                return true;
            });
            provider.setOnGenerateNote(note -> {
                new FileAccessLayerFactory().get().getAllNotes().add(note);
                try {
                    reloadCampaign();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return true;
            });
            tmp.setText(provider.menuItemLabel());
            // FIXME: Only provide a copy!
            tmp.setOnAction(event -> provider.startTask(new FileAccessLayerFactory().get().getFile(), parentWnd, new FileAccessLayerFactory().get()));
            this.dataProviders.getItems().add(tmp);
        });

        SettingsDialog dialog = new SettingsDialog();
        openSettingsMenuItem.onActionProperty().set(e -> {
            dialog.show(parentWnd);
        });

        openLastViewer();
    }

    void switchViewer(Toggle present) {
        Scene editor = mapping.get(present).getValue();
        if (editor != null) {
            activeInterface = mapping.get(present).getKey();
            new FileAccessLayerFactory().get().updateSetting("lastModule", activeInterface.getMenuItemLabel());

            inset.setCenter(editor.getRoot());
            activeInterface.requestLoad(new FileAccessLayerFactory().get().getFile());
            activeInterface.requestLoadNote(param -> {

                new FileAccessLayerFactory().get().findByReference(param).ifPresent(foundNote -> {
                    System.out.println("request to open " + foundNote + " as type " + foundNote.getType());
                    Registry.getInstance().getEditorBySuffix(foundNote.getType()).ifPresent(editorModule -> {
                        mapping.entrySet().stream().filter(entryKey -> {
                            return editorModule.defineHandler().split("/")[0].equals(entryKey.getValue().getKey().defineViewerHandlerPrefix());
                        }).findFirst().ifPresent(moduleEntry -> {
                            moduleEntry.getKey().setSelected(true);
                            moduleEntry.getValue().getKey().openNote(foundNote);

                        });

                    });
                });
                return true;
            });
        }
    }


    @FXML
    public void createNewCampaign() {
        // unsaved data check
        this.currentFile = null;
        new FileAccessLayerFactory().get().newCampaign();

        activeInterface.requestLoad(new FileAccessLayerFactory().get().getFile());
    }

    private void openLastViewer() {
        new FileAccessLayerFactory().get().getSetting("lastModule").ifPresentOrElse(campaignSettingLastViewer -> {
            viewMode.getToggles().stream().filter(t -> ((RadioMenuItem) t).getText().equals(campaignSettingLastViewer)).findFirst().ifPresent(toggle -> {
                switchViewer(toggle);
                viewMode.selectToggle(toggle);
            });
        }, () -> {
            viewMode.selectToggle(viewMode.getToggles().get(0));

        });

    }

    public void openCampaign(File newFile) {
        this.currentFile = newFile;
        if (newFile != null && newFile.exists()) {
            try {
                new FileAccessLayerFactory().get().loadFromFile(this.currentFile);

                if (this.currentFile == null) {
                    throw new IOException();
                }

                if (activeInterface != null) {
                    activeInterface.requestLoad(new FileAccessLayerFactory().get().getFile());
                }
                title.set(this.currentFile.getName());

                openLastViewer();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "There has an error occurred while trying to open the campaign file").show();
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void openCampaign() throws IOException {
        FileChooser dialog = new FileChooser();
        dialog.setTitle(i18n.getString("openFileDialogTitle"));

        dialog.getExtensionFilters().setAll(supportedFileExtensions);
        File newFile = dialog.showOpenDialog(null);
        this.currentFile = newFile;
        if (newFile != null) {
            try {
                new FileAccessLayerFactory().get().loadFromFile(this.currentFile);
                if (activeInterface != null) {
                    activeInterface.requestLoad(new FileAccessLayerFactory().get().getFile());
                }
                title.set(this.currentFile.getName());

                openLastViewer();
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "There has an error occurred while trying to open the campaign file").show();
                e.printStackTrace();
            }
        }
    }

    public void reloadCampaign() throws IOException {
        // save current note just in case
        if (activeInterface != null) {
            activeInterface.requestSave();
            new FileAccessLayerFactory().get().saveToFile();
            try {
                new FileAccessLayerFactory().get().loadFromFile(this.currentFile);
                activeInterface.requestLoad(new FileAccessLayerFactory().get().getFile());
            } catch (IOException e) {
                new Alert(Alert.AlertType.ERROR, "There has an error occurred while trying to open the campaign file").show();
                e.printStackTrace();
            }
        }
    }

    @FXML
    public void saveCampaign() throws IOException {
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
            new FileAccessLayerFactory().get().saveToFile(this.currentFile);
            title.set(this.currentFile.getName());
        }
    }

    public void setStage(Stage parentWnd) {
        this.parentWnd = parentWnd;
    }

    public void closeApplication(ActionEvent actionEvent) {
        this.parentWnd.close();
    }
}