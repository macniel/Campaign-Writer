package de.macniel.campaignwriter.modules;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.SDK.*;
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
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class WorldBuildingModule extends ModulePlugin {

    private ObservableList<Note> notes;

    private Note activeNote;

    @FXML
    public ListView<Note> notesLister;

    @FXML
    public TextArea editor;
    private EditorPlugin lastCreationAction;

    @FXML
    private SplitMenuButton creationMenuButton;

    private ArrayList<EditorPlugin> plugins;

    int dragPosition;
    Note dragElement;

    private Stage stage;

    private Callback<UUID, Boolean> requester;

    private ResourceBundle i18n;
    private int lastNote;


    @Override
    public void requestLoadNote(Callback<UUID, Boolean> cb) {
        this.requester = cb;
    }

    @Override
    public String getPathToFxmlDefinition() {
        return "building-view.fxml";
    }


    public static String getLocalizationBase() {
        return "i18n.buildingview";
    }

    @Override
    public String defineViewerHandlerPrefix() {
        return "building";
    }

    @Override
    public String getMenuItemLabel() {
        return i18n.getString("WorldbuildingViewMenuItem");
    }


    @Override
    public void requestSave() {
        if (activeNote != null) {

            Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/" + activeNote.getType()).ifPresent(oe -> {
                Callback<Boolean, Note<?>> saveEditor = oe.defineSaveCallback();
                Note<?> res = saveEditor.call(true);
                System.out.println("Saving note as type " + res.getClass().toString());
                int insertionPoint = FileAccessLayer.getInstance().getAllNotes().indexOf(activeNote);
                if (insertionPoint >= 0) {
                    FileAccessLayer.getInstance().getAllNotes().remove(activeNote);
                    FileAccessLayer.getInstance().getAllNotes().add(insertionPoint, res);
                } else {
                    FileAccessLayer.getInstance().getAllNotes().add(res);
                }
                try {
                    FileAccessLayer.getInstance().saveToFile();
                } catch (IOException e) {
                }
            });
        }
    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {

    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerModule(this);
    }

    public WorldBuildingModule() {
        super();
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    @Override
    public void requestLoad(CampaignFileInterface file) {
        if (notesLister != null) {

            updateLister();
            String lastLoadedNote = FileAccessLayer.getInstance().getSetting("lastNote");
            if (lastLoadedNote != null) {
                FileAccessLayer.getInstance().getAllNotes().stream().filter(sn -> sn.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                    activeNote = note;
                    notesLister.getSelectionModel().select(activeNote);
                });
            }
        }
    }
    @FXML
    public void initialize() {

        plugins = Registry.getInstance().getEditorsByPrefix("building");

        List<Note> listOfBuildingNotes = FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().startsWith("building")).toList();

                System.out.println("reading " + listOfBuildingNotes.size() + " of " + FileAccessLayer.getInstance().getAllNotes().size() + " notes");

        notes = FXCollections.observableArrayList(listOfBuildingNotes);

        notesLister.setItems(notes);

        notesLister.setCellFactory(listView -> {
            ListCell<Note> t = new NotesRenderer();

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
                    updateLister();
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
        MenuItem deleteNoteMenuItem = new MenuItem(i18n.getString("DeleteNote"));
        deleteNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            FileAccessLayer.getInstance().removeNote(contextedNote);
            try {
                FileAccessLayer.getInstance().saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateLister();
            notesLister.refresh();
        });
        MenuItem renameNoteMenuItem = new MenuItem(i18n.getString("RenameNote"));
        renameNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();

            TextInputDialog input = new TextInputDialog();
            input.setTitle(String.format(i18n.getString("RenameDialogTitle"), contextedNote.getLabel()));
            Optional<String> result = input.showAndWait();
            contextedNote.setLabel(result.get());
            notesLister.refresh();
        });
        MenuItem indentNoteMenuItem = new MenuItem(i18n.getString("IndentNote"));
        indentNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel()+1);
            notesLister.refresh();
        });
        MenuItem deindentNoteMenuItem = new MenuItem(i18n.getString("DeindentNote"));
        deindentNoteMenuItem.onActionProperty().set( event -> {
            Note contextedNote = (Note) notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel()-1);
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
                if (old != selected && selected != null) {
                    lastNote = notesLister.getSelectionModel().getSelectedIndex();

                    FileAccessLayer.getInstance().updateSetting("lastNote", selected.getReference().toString());
                    saveAndLoad(old, selected);
                }
            }
        });

        creationMenuButton.getItems().clear();
        plugins.forEach(plugin -> {
            MenuItem tmp = new MenuItem();
            tmp.setText(plugin.defineHandler());
            tmp.onActionProperty().set( (ActionEvent e) -> {
                createNote(plugin);
            });
            creationMenuButton.getItems().add(tmp);
        });

    }

    public void createNote(EditorPlugin editor) {

        Note newNote = editor.createNewNote();

        FileAccessLayer.getInstance().getAllNotes().add(newNote);
        notesLister.getItems().add(newNote);
        notesLister.getSelectionModel().select(newNote);
        saveAndLoad(activeNote, newNote);
        lastCreationAction = editor;
        creationMenuButton.setText(newNote.getType());
    }

    public void saveAndLoad(final Note<?> oldNote, Note<?> newNote) {

        if (oldNote != null) {

            Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/" + oldNote.getType()).ifPresent(oe -> {
                Callback<Boolean, Note<?>> saveEditor = oe.defineSaveCallback();
                Note<?> res = saveEditor.call(true);
                System.out.println("Saving note as type " + res.getClass().toString());
                int insertionPoint = FileAccessLayer.getInstance().getAllNotes().indexOf(oldNote);
                if (insertionPoint >= 0) {
                    FileAccessLayer.getInstance().getAllNotes().remove(oldNote);
                    FileAccessLayer.getInstance().getAllNotes().add(insertionPoint, res);
                } else {
                    FileAccessLayer.getInstance().getAllNotes().add(res);
                }
                try {
                    FileAccessLayer.getInstance().saveToFile();
                } catch (IOException e) {}
            });
        }
        Optional<EditorPlugin> newEditor = Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/"+ newNote.getType());



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
            updateLister();
            try {
                FileAccessLayer.getInstance().saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void updateLister() {
        notesLister.setItems(FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(n -> Registry.getInstance().getEditorByFullName("building/" + n.getType()).isPresent()).toList()));

        notesLister.getSelectionModel().select(Math.min(lastNote, notesLister.getItems().size()-1));
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
