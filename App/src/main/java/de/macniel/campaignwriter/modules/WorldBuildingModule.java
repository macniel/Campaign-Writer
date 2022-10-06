package de.macniel.campaignwriter.modules;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.TextNote;
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
    public TreeView<Note> notesLister;
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

    private TreeItem<Note> findInTree(TreeItem<Note> root, Note toFind) {
        System.out.println("Entering " + root + " with " + root.getChildren().size() + " children");
        if (root.getValue() != null &&
                root.getValue().getReference().equals(toFind.getReference())) {
            System.out.println("Found " + root.getValue().getLabel());
            return root;
        }
        if (root.getChildren().size() > 0) {
            for (TreeItem<Note> child : root.getChildren()) {
                System.out.println("visting " + child);
                TreeItem<Note> tmp = findInTree(child, toFind);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

    @Override
    public void requestLoad(CampaignFileInterface file) {
        if (notesLister != null) {

            updateLister();
            new FileAccessLayerFactory().get().getSetting("lastNote").ifPresent(lastLoadedNote -> {
                new FileAccessLayerFactory().get().getAllNotes().stream().filter(sn -> sn.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                    activeNote = note;
                    TreeItem<Note> activeItem = findInTree(notesLister.getRoot(), activeNote);
                    if (activeItem != null) {
                        notesLister.getSelectionModel().select(activeItem);
                    }
                });
            });
        }
    }

    @FXML
    public void initialize() {

        plugins = Registry.getInstance().getEditorsByPrefix("building");
        TreeItem tmpTarget = new TreeItem();
        final boolean[] isTmpTargetSet = {false};

        // before, after, inside?
        notesLister.setCellFactory(listView -> {
            TreeCell<Note> t = new NotesTreeRenderer();

            tmpTarget.setGraphic(new Label("Drop here"));

            t.onDragOverProperty().set(e -> {
                dragPosition = t.getIndex();
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });

            t.onDragExitedProperty().set(e -> {
                if (tmpTarget.getParent() != null && !tmpTarget.getParent().equals(t.getTreeItem())) {
                    if (t.getTreeItem() != null) {
                        t.getTreeItem().getChildren().remove(tmpTarget);
                        isTmpTargetSet[0] = false;
                    }
                }
            });

            t.onDragEnteredProperty().set(e -> {
                if (!dragElement.equals(t.getItem()) && t.getTreeItem() != null
                        && !tmpTarget.equals(t.getTreeItem())
                        && !tmpTarget.equals(tmpTarget.getParent())) {
                    if (!isTmpTargetSet[0]) {
                        t.getTreeItem().getChildren().add(tmpTarget);
                        isTmpTargetSet[0] = true;
                        t.getTreeItem().setExpanded(true);
                    }
                }
                dragPosition = t.getIndex();
                e.acceptTransferModes(TransferMode.MOVE);
                e.consume();
            });

            t.onDragDroppedProperty().set(e -> {
                if (dragElement != null) {

                    TreeItem<Note> draggedItem = findInTree(notesLister.getRoot(), dragElement);

                    if (draggedItem.isLeaf()) {
                        new FileAccessLayerFactory().get().removeNote(dragElement);
                        new FileAccessLayerFactory().get().addNote(dragPosition, dragElement);
                    } else {
                        // TODO: move the entire subtree
                    }

                    if (tmpTarget.equals(t.getTreeItem())) {
                        System.out.println("dropped onto drop-placeholder");
                        dragElement.setLevel(t.getTreeItem().getParent().getValue().getLevel() + 1);

                        updateLister();
                    } else if (notesLister.getRoot().equals(t.getTreeItem())) {
                        System.out.println("dropped onto root");
                        dragElement.setLevel(0);

                        updateLister();
                    } else {
                        System.out.println("dropped onto another element");
                        dragElement.setLevel(t.getItem().getLevel());
                        updateLister();
                    }

                    TreeItem<Note> child = findInTree(notesLister.getRoot(), dragElement);
                    if (child != null) {
                        child.getParent().setExpanded(true);
                    }
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
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem().getValue();
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
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem().getValue();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();

            TextInputDialog input = new TextInputDialog();
            input.setTitle(String.format(i18n.getString("RenameDialogTitle"), contextedNote.getLabel()));
            Optional<String> result = input.showAndWait();
            contextedNote.setLabel(result.get());
            notesLister.refresh();
        });
        MenuItem indentNoteMenuItem = new MenuItem(i18n.getString("IndentNote"));
        indentNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem().getValue();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel() + 1);
            updateLister();
        });
        MenuItem deindentNoteMenuItem = new MenuItem(i18n.getString("DeindentNote"));
        deindentNoteMenuItem.onActionProperty().set(event -> {
            Note contextedNote = notesLister.getSelectionModel().getSelectedItem().getValue();
            lastNote = notesLister.getSelectionModel().getSelectedIndex();
            contextedNote.setLevel(contextedNote.getLevel() - 1);
            updateLister();
        });
        notesListerMenu.getItems().add(renameNoteMenuItem);
        notesListerMenu.getItems().add(indentNoteMenuItem);
        notesListerMenu.getItems().add(deindentNoteMenuItem);
        notesListerMenu.getItems().add(new SeparatorMenuItem());
        notesListerMenu.getItems().add(deleteNoteMenuItem);
        notesLister.setContextMenu(notesListerMenu);

        notesLister.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Note>>() {

            @Override
            public void changed(ObservableValue<? extends TreeItem<Note>> observable, TreeItem<Note> oldValue, TreeItem<Note> newValue) {
                if (newValue != null && oldValue != null &&
                        newValue.getValue() != null &&
                        oldValue.getValue() != null &&
                        oldValue.getValue() != newValue.getValue()) {
                    lastNote = notesLister.getSelectionModel().getSelectedIndex();

                    new FileAccessLayerFactory().get().updateSetting("lastNote", newValue.getValue().getReference().toString());
                    saveAndLoad(oldValue.getValue(), newValue.getValue());
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
        updateLister();
    }

    public void createNote(EditorPlugin editor) {

        Note newNote = editor.createNewNote();
        new FileAccessLayerFactory().get().getAllNotes().add(newNote);


        if (notesLister.getSelectionModel().getSelectedItem() != null &&
                notesLister.getSelectionModel().getSelectedItem().getValue() != null) {
            newNote.setLevel(notesLister.getSelectionModel().getSelectedItem().getValue().getLevel() + 1);
            notesLister.getSelectionModel().getSelectedItem().getChildren().add(new TreeItem<>(newNote));
            notesLister.getSelectionModel().getSelectedItem().setExpanded(true);
        } else {
            newNote.setLevel(0);
            notesLister.getRoot().getChildren().add(new TreeItem<>(newNote));
        }
        //updateLister();
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
        Note selectedNote = notesLister.getSelectionModel().getSelectedItem().getValue();
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
        List<Note> listOfBuildingNotes = new FileAccessLayerFactory().get().getAllNotes();

        System.out.println("reading " + listOfBuildingNotes.size() + " of " + new FileAccessLayerFactory().get().getAllNotes().size() + " notes");

        final TreeItem<Note> root = new TreeItem<>();

        final TreeItem<Note>[] lastChild = new TreeItem[]{null};
        final Note[] previousNote = {null};
        listOfBuildingNotes.forEach(note -> {
            System.out.println("prev " + previousNote[0] + " curr " + note);
            if (previousNote[0] != null && lastChild[0] != null) {
                if (note.getLevel() == previousNote[0].getLevel()) { // same level add as
                    TreeItem<Note> tmp = new TreeItem<>(note);
                    System.out.println("same level as previous, add as sibling");
                    lastChild[0].getParent().getChildren().add(tmp);
                    lastChild[0] = tmp;
                } else {
                    if (note.getLevel() > previousNote[0].getLevel()) { // child of last
                        System.out.println("higher than previous, so add as child");
                        TreeItem<Note> tmp = new TreeItem<>(note);
                        lastChild[0].getChildren().add(tmp);
                        lastChild[0] = tmp;
                    } else { // belongs to parent
                        System.out.println("lower than previous, so add to previous parents parents child");
                        TreeItem<Note> tmp = new TreeItem<>(note);
                        lastChild[0].getParent().getParent().getChildren().add(tmp);
                        lastChild[0] = tmp;
                    }
                }
            } else {
                System.out.println("add " + note + " into root");
                lastChild[0] = new TreeItem<>(note);
                root.getChildren().add(lastChild[0]);
            }
            previousNote[0] = note;
        });
        notesLister.setShowRoot(true);
        notesLister.setRoot(root);
        root.setExpanded(true);

    }

    void setStage(Stage stage) {
        this.stage = stage;
    }

}
