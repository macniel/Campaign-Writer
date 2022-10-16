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

    private final ResourceBundle i18n;
    @FXML
    public ListView<Note> notesLister;
    @FXML
    public TextArea editor;
    int dragPosition;
    Note dragElement;
    private ObservableList<Note> notes;
    private Note activeNote;
    private EditorPlugin lastCreationAction;
    @FXML
    private SplitMenuButton creationMenuButton;
    private ArrayList<EditorPlugin> plugins;
    private Stage stage;
    private Callback<UUID, Boolean> requester;
    private int lastNote;
    @FXML
    private BorderPane editorWindow;
    @FXML
    private ToolBar editorToolbar;


    public WorldBuildingModule() {
        super();
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    public static String getLocalizationBase() {
        return "i18n.buildingview";
    }

    @Override
    public void requestLoadNote(Callback<UUID, Boolean> cb) {
        this.requester = cb;
    }

    @Override
    public String getPathToFxmlDefinition() {
        return "building-view.fxml";
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
    public void register(RegistryInterface registry) {
        registry.registerModule(this);
    }

    @Override
    public void requestLoad(CampaignFileInterface file) {
        if (notesLister != null) {

            updateLister();
            new FileAccessLayerFactory().get().getSetting("lastNote").ifPresent(lastLoadedNote -> {
                new FileAccessLayerFactory().get().getAllNotes().stream().filter(sn -> sn.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                    activeNote = note;
                    notesLister.getSelectionModel().select(activeNote);
                });
            });
        }
    }

    @FXML
    public void initialize() {

        plugins = Registry.getInstance().getEditorsByPrefix("building");

        List<Note> listOfBuildingNotes = new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().startsWith("building")).toList();

        System.out.println("reading " + listOfBuildingNotes.size() + " of " + new FileAccessLayerFactory().get().getAllNotes().size() + " notes");

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
                    new FileAccessLayerFactory().get().removeNote(dragElement);
                    new FileAccessLayerFactory().get().addNote(dragPosition, dragElement);
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
        deleteNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            new FileAccessLayerFactory().get().removeNote(contextedNote);
            try {
                new FileAccessLayerFactory().get().saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            updateLister();
            notesLister.refresh();
        });
        MenuItem renameNoteMenuItem = new MenuItem(i18n.getString("RenameNote"));
        renameNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();

            TextInputDialog input = new TextInputDialog();
            input.setTitle(String.format(i18n.getString("RenameDialogTitle"), contextedNote.getLabel()));
            Optional<String> result = input.showAndWait();
            contextedNote.setLabel(result.get());
            notesLister.refresh();
        });
        MenuItem indentNoteMenuItem = new MenuItem(i18n.getString("IndentNote"));
        indentNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel() + 1);
            notesLister.refresh();
        });
        MenuItem deindentNoteMenuItem = new MenuItem(i18n.getString("DeindentNote"));
        deindentNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel() - 1);
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

                    new FileAccessLayerFactory().get().updateSetting("lastNote", selected.getReference().toString());
                    saveAndLoad(old, selected);
                }
            }
        });

        creationMenuButton.getItems().clear();
        plugins.forEach(plugin -> {
            MenuItem tmp = new MenuItem();
            tmp.setText(plugin.defineHandler());
            tmp.onActionProperty().set((ActionEvent e) -> {
                createNote(plugin);
            });
            creationMenuButton.getItems().add(tmp);
        });

    }

    public void openNote(Note toOpen) {
        System.out.println("OpenNote in Module WorldBuilding" + toOpen.getType());
        //saveAndLoad(null, toOpen);
        notesLister.getSelectionModel().select(toOpen);
    }

    public void createNote(EditorPlugin editor) {

        Note newNote = editor.createNewNote();

        new FileAccessLayerFactory().get().getAllNotes().add(newNote);
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
                if (res != null) {
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
                    } catch (IOException e) {
                    }
                }
            });
        }
        Optional<EditorPlugin> newEditor = Registry.getInstance().getEditorByFullName(this.defineViewerHandlerPrefix() + "/" + newNote.getType());
        System.out.println(newEditor);

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
    public void createNewNote(ActionEvent event) {
        if (lastCreationAction != null) {
            createNote(lastCreationAction);
        }
    }

    @FXML
    public void deleteCurrentNote() {
        Note selectedNote = notesLister.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            new FileAccessLayerFactory().get().removeNote(selectedNote);
            updateLister();
            try {
                new FileAccessLayerFactory().get().saveToFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    void updateLister() {
        notesLister.setItems(FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(n -> Registry.getInstance().getEditorByFullName("building/" + n.getType()).isPresent()).toList()));

        notesLister.getSelectionModel().select(Math.min(lastNote, notesLister.getItems().size() - 1));
    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
