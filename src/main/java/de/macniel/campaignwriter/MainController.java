package de.macniel.campaignwriter;

import de.macniel.campaignwriter.editors.EditorPlugin;
import de.macniel.campaignwriter.editors.PictureNoteEditor;
import de.macniel.campaignwriter.editors.TextNoteEditor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MainController {

    private ObservableList<String> sortingOptions;

    private ObservableList<Note> notes;

    private boolean contentHasChanged = false;

    private Note activeNote;

    private File currentFile;

    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;

    @FXML
    public ComboBox sortlistSelector;

    @FXML
    public Button createPageButton;

    @FXML
    public ListView notesLister;

    @FXML
    public TextArea editor;
    private NoteType lastCreationAction;

    @FXML
    private SplitMenuButton creationMenuButton;
    private ArrayList<EditorPlugin> plugins;
    private Stage stage;

    @FXML
    public void initialize() {
        sortingOptions = FXCollections.observableArrayList(
                "By Typ","By Name", "By Creation Date", "By Modification Date"
        );
        sortlistSelector.setItems(sortingOptions);

        notes = FXCollections.observableArrayList();

        plugins = new ArrayList<>();
        plugins.add(new TextNoteEditor());
        plugins.add(new PictureNoteEditor());

        notesLister.setItems(notes);
        notesLister.setCellFactory(listView -> {
            return new NotesRenderer();
        });

        ContextMenu notesListerMenu = new ContextMenu();
        MenuItem deleteNoteMenuItem = new MenuItem("Löschen");
        deleteNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            Note.remove(contextedNote);
            notesLister.getItems().remove(contextedNote);
            notesLister.refresh();
        });
        MenuItem renameNoteMenuItem = new MenuItem("Umbenenen");
        renameNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();

            TextInputDialog input = new TextInputDialog();
            input.setTitle("Neuer Name der Notiz " + contextedNote.getLabel());
            Optional<String> result = input.showAndWait();
            contextedNote.setLabel(result.get());
            notesLister.refresh();
        });
        MenuItem indentNoteMenuItem = new MenuItem("Einrücken");
        indentNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            contextedNote.increaseLevel();
            notesLister.refresh();
        });
        MenuItem deindentNoteMenuItem = new MenuItem("Ausrücken");
        deindentNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            contextedNote.decreaseLevel();
            notesLister.refresh();
        });
        notesListerMenu.getItems().add(renameNoteMenuItem);
        notesListerMenu.getItems().add(indentNoteMenuItem);
        notesListerMenu.getItems().add(deindentNoteMenuItem);
        notesListerMenu.getItems().add(new SeparatorMenuItem());
        notesListerMenu.getItems().add(deleteNoteMenuItem);
        notesLister.setContextMenu(notesListerMenu);

        notesLister.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Note>() {
            @Override
            public void changed(ObservableValue observableValue, Note old, Note selected) {
                System.out.println("Changing View");
                if (old != selected) {
                    saveAndLoad(old, selected);
                }
            }
        });

        creationMenuButton.getItems().clear();
        for (NoteType value : NoteType.values()) {
            MenuItem tmp = new MenuItem();
            tmp.setText(value.label);
            tmp.onActionProperty().set( (ActionEvent e) -> {
                createNote(value);
            });
            creationMenuButton.getItems().add(tmp);
        }
        supportedFileExtensions = new ArrayList<>();
        supportedFileExtensions.add(new FileChooser.ExtensionFilter("campaign file", "*.campaign"));
    }

    private EditorPlugin getByType (NoteType type) {
        try {
            return plugins.stream().filter( editorPlugin -> editorPlugin.defineHandler() == type).findFirst().get();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    public void createNote(NoteType type) {
        Note newNote = new Note(type.label, type, UUID.randomUUID(), new Date(), new Date(), "");
        newNote.setPosition(Note.getAll().size());
        notesLister.getItems().add(newNote);
        saveAndLoad(activeNote, newNote);
        lastCreationAction = type;
        creationMenuButton.setText(type.label);
    }

    public void saveAndLoad(Note oldNote, Note newNote) {
        boolean saveOkay = false;
        boolean loadOkay = false;


        EditorPlugin oldEditor = null;
        EditorPlugin newEditor = null;
        if (oldNote != null) {
            oldEditor = getByType(oldNote.getType());
        }
        if (newNote != null) {
            newEditor = getByType(newNote.getType());
        }


        if (oldEditor != null) {
            Callback<Note, Boolean> saveEditor = oldEditor.defineSaveCallback();
            saveOkay = saveEditor.call(oldNote);
        }

        if (newEditor != null) {
            Node editor = newEditor.defineEditor();
            newEditor.prepareToolbar(editorToolbar, stage);
            editorWindow.setContent(editor);

            Callback<Note, Boolean> loadEditor = newEditor.defineLoadCallback();
            loadOkay = loadEditor.call(newNote);
        }
    }

    @FXML
    private ScrollPane editorWindow;


    @FXML
    private ToolBar editorToolbar;

    @FXML public void createNewNote(ActionEvent event) {
       if (lastCreationAction != null) {
           createNote(lastCreationAction);
       }
    }

    @FXML public void deleteCurrentNote() {
        Note selectedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            Note.remove(selectedNote);
            notesLister.getItems().remove(selectedNote);
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML public void createNewCampaign() throws IOException {
        // unsaved data check
        this.currentFile = null;

        Note.removeAll();
        notesLister.setItems(FXCollections.observableArrayList(Note.getAll()));

    }

    @FXML public void openCampaign() throws IOException {
        FileChooser dialog = new FileChooser();
        dialog.setTitle("Kampagne zum Öffnen auswählen");

        dialog.getExtensionFilters().setAll(supportedFileExtensions);
        File newFile = dialog.showOpenDialog(stage);
        this.currentFile = newFile;
        if (newFile != null) {
            FileAccessLayer.loadFromFile(this.currentFile);
            notesLister.setItems(FXCollections.observableArrayList(Note.getAll()));
        }
    }

    @FXML public void saveCampaign() throws IOException {
        // save current note just in case
        if (activeNote != null) {
            getByType(activeNote.getType()).defineSaveCallback().call(activeNote);
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