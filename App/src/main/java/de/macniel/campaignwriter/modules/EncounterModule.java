package de.macniel.campaignwriter.modules;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

import de.macniel.campaignwriter.EncounterNoteRenderer;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Registry;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.EncounterNote;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

public class EncounterModule extends ModulePlugin {
    private final ResourceBundle i18n;
    @FXML
    private ListView<EncounterNote> notesLister;
    @FXML
    private ToolBar editorToolbar;

    @FXML
    private BorderPane editorWindow;

    private Callback<UUID, Boolean> requester;

    EncounterNote activeNote;

    List<EncounterNote> encounters;
    private Stage stage;

    @Override
    public String getPathToFxmlDefinition() {
        return "encounter-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return i18n.getString("EncounterViewMenuItem");
    }

    @Override
    public void requestLoad(CampaignFileInterface items) {
        if (items != null) {
            encounters = items.getNotes().stream().filter(note -> note.getType().endsWith("encounter")).map(e -> (EncounterNote) e).toList();
            notesLister.setItems(FXCollections.observableArrayList(encounters));

            String lastLoadedNote = new FileAccessLayerFactory().get().getSetting("lastNote");
            if (lastLoadedNote != null) {
                new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().endsWith("encounter")).filter(n -> n.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                    activeNote = (EncounterNote) note;
                    notesLister.getSelectionModel().select(activeNote);
                });
            }
        }
    }

    @Override
    public void requestSave() {
        if (activeNote != null) {
            Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/" + activeNote.getType()).ifPresent(oe -> {
                Callback<Boolean, Note<?>> saveEditor = oe.defineSaveCallback();
                Note<?> res = saveEditor.call(true);
                System.out.println("Saving note as type " + res.getClass().toString());
                int insertionPoint = new FileAccessLayerFactory().get().getAllNotes().indexOf(activeNote);
                if (insertionPoint >= 0) {
                    new FileAccessLayerFactory().get().getAllNotes().remove(activeNote);
                    new FileAccessLayerFactory().get().getAllNotes().add(insertionPoint, res);
                } else {
                    new FileAccessLayerFactory().get().getAllNotes().add(res);
                }
                try {
                    new FileAccessLayerFactory().get().saveToFile();
                } catch (IOException e) {
                }
            });
        }
    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {

    }

    @Override
    public void requestLoadNote(Callback<UUID, Boolean> cb) {
        this.requester = cb;
    }

    public static String getLocalizationBase() {
        return "i18n.encounters";
    }

    @Override
    public String defineViewerHandlerPrefix() {
        return "encounter";
    }

    public EncounterModule() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }


    @FXML
    public void initialize() {

        notesLister.setCellFactory(noteListView -> new EncounterNoteRenderer());


        notesLister.getSelectionModel().selectedItemProperty().addListener( (observableValue, oldNote, newNote) -> {
            if (newNote != null) {
                activeNote = newNote;
                new FileAccessLayerFactory().get().updateSetting("lastNote", newNote.getReference().toString());
                saveAndLoad(oldNote, newNote);
            }
        });
    }

    private void saveAndLoad(EncounterNote oldNote, EncounterNote newNote) {

        if (oldNote != null) {

            Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/" + oldNote.getType()).ifPresent(oe -> {
                Callback<Boolean, Note<?>> saveEditor = oe.defineSaveCallback();
                Note<?> res = saveEditor.call(true);
                System.out.println("Saving note as type " + res.getClass().toString());
                int insertionPoint = new FileAccessLayerFactory().get().getAllNotes().indexOf(oldNote);
                if (insertionPoint >= 0) {
                    new FileAccessLayerFactory().get().getAllNotes().remove(oldNote);
                    new FileAccessLayerFactory().get().getAllNotes().add(insertionPoint, res);
                } else {
                    new FileAccessLayerFactory().get().getAllNotes().add(res);
                }
                try {
                    new FileAccessLayerFactory().get().saveToFile();
                } catch (IOException e) {}
            });
        }
        Optional<EditorPlugin> newEditor = Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/"+ newNote.getType());
        System.out.println("found editor " + newEditor + " for requested type " + this.defineViewerHandlerPrefix() + "/" + newNote.getType());


        newEditor.ifPresent(ne -> {
            Node editor = ne.defineEditor();
            ne.prepareToolbar(editorToolbar, stage);
            editorWindow.setCenter(editor);

            Callback<Note<?>, Boolean> loadEditor = ne.defineLoadCallback();
            loadEditor.call(newNote);
            activeNote = newNote;

            ne.setOnNoteLoadRequest((Callback<String, Boolean>) param -> {
                Boolean found = requester.call(UUID.fromString(param));
                if (found) {
                    saveAndLoad(newNote, null);
                }
                return found;
            });

        });
    }

    public void newEncounter(ActionEvent actionEvent) {
        EncounterNote n = new EncounterNote();
        n.getContentAsObject().setEncounterName(i18n.getString("UntitledEncounter"));
        new FileAccessLayerFactory().get().getAllNotes().add(n);
        encounters = new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().endsWith("encounter")).map(e -> (EncounterNote) e).toList();
        requestSave();
        notesLister.setItems(FXCollections.observableArrayList(encounters));
        activeNote = n;
    }

    public void deleteEncounter(ActionEvent actionEvent) {
        if (activeNote != null && encounters.contains(activeNote)) {
            new FileAccessLayerFactory().get().getAllNotes().remove(activeNote);
            encounters.remove(activeNote);
            activeNote = null;
            encounters = new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().endsWith("encounter")).map(e -> (EncounterNote) e).toList();
            notesLister.setItems(FXCollections.observableArrayList(encounters));

            notesLister.getSelectionModel().clearSelection();
        }
    }

    public void beginEncounter(ActionEvent actionEvent) {
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerModule(this);
    }


    void setStage(Stage stage) {
        this.stage = stage;
    }
}

