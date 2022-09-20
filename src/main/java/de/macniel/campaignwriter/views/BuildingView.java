package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.editors.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.*;

public class BuildingView implements ViewInterface {

    private ObservableList<Note> notes;

    private Note activeNote;

    @FXML
    public ListView<Note> notesLister;

    @FXML
    public TextArea editor;
    private NoteType lastCreationAction;

    @FXML
    private SplitMenuButton creationMenuButton;

    private ArrayList<EditorPlugin> plugins;


    int dragPosition;
    Note dragElement;

    private Stage stage;

    private ArrayList<FileChooser.ExtensionFilter> supportedFileExtensions;
    private Callback<UUID, Note> requester;

    public void setPlugins(ArrayList<EditorPlugin> plugins) {
        this.plugins = plugins;
    }

    public void requestSave() {
        getByType(activeNote.getType()).defineSaveCallback().call(activeNote);
    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }

    @Override
    public String getPathToFxmlDefinition() {
        return "building-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return "Worldbuilding";
    }

    public void requestLoad(CampaignFile file) {
        if (notesLister != null) {
            System.out.println("Loading " + file.notes.size() + " notes");
            notesLister.setItems(FXCollections.observableArrayList(file.notes));
        }
    }
    @FXML
    public void initialize() {

        plugins = new ArrayList<>();
        plugins.add(new TextNoteEditor());
        plugins.add(new PictureNoteEditor());
        plugins.add(new MapNoteEditor());
        plugins.add(new ActorEditor());

        notes = FXCollections.observableArrayList();

        notesLister.setItems(notes);

        notesLister.setCellFactory(listView -> {
            ListCell<Note> t = new NotesRenderer();

            Note draggedElement;

            t.onDragOverProperty().set(e -> {
                dragPosition = t.getIndex();
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });

            t.onDragExitedProperty().set(e -> {

            });

            t.onDragEnteredProperty().set(e -> {
                dragPosition = t.getIndex();
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });

            t.onDragDroppedProperty().set(e -> {
                if (dragElement != null) {
                    FileAccessLayer.getInstance().removeNote(dragElement);
                    FileAccessLayer.getInstance().addNote(dragPosition, dragElement);
                    notesLister.setItems(FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes()));
                }
                e.setDropCompleted(true);
                e.consume();
            });


            t.onDragDetectedProperty().set(e -> {
                dragElement = t.getItem();
                dragPosition = t.getIndex();
                Dragboard db = t.startDragAndDrop(TransferMode.ANY);
                ClipboardContent c = new ClipboardContent();
                c.putString("accepted");
                db.setContent(c);

                e.consume();
            });


            return t;
        });

        ContextMenu notesListerMenu = new ContextMenu();
        MenuItem deleteNoteMenuItem = new MenuItem("Löschen");
        deleteNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            FileAccessLayer.getInstance().removeNote(contextedNote);
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
        FileAccessLayer.getInstance().getAllNotes().add(newNote);
        notesLister.getItems().add(newNote);
        notesLister.getSelectionModel().select(newNote);
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
            editorWindow.setCenter(editor);

            Callback<Note, Boolean> loadEditor = newEditor.defineLoadCallback();
            loadOkay = loadEditor.call(newNote);
            activeNote = newNote;

            newEditor.setOnNoteRequest(new Callback<String, Note>() {
                @Override
                public Note call(String param) {
                    return requester.call(UUID.fromString(param));
                }
            });

            newEditor.setOnNoteLoadRequest(new Callback<String, Boolean>() {
                @Override
                public Boolean call(String param) {
                    Note found = requester.call(UUID.fromString(param));
                    if (found != null) {
                        notesLister.getSelectionModel().select(found);
                        saveAndLoad(newNote, found);
                        return true;
                    }
                    return false;
                }
            });

        }
    }

    @FXML
    private BorderPane editorWindow;


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
            FileAccessLayer.getInstance().removeNote(selectedNote);
            notesLister.getItems().remove(selectedNote);
        }
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
