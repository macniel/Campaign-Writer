package de.macniel.campaignwriter.modules;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.types.SessionNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class SessionModule extends ModulePlugin {

    private final ResourceBundle i18n;
    public ToolBar toolBar;
    private Callback<UUID, Boolean> onNoteLoadRequest;

    @Override
    public void requestLoadNote(Callback<UUID, Boolean> cb) {
        this.onNoteLoadRequest = cb;
    }

    @Override
    public String getPathToFxmlDefinition() {
        return "session-view.fxml";
    }

    public static String getLocalizationBase() {
        return "i18n.sessions";
    }

    public SessionModule() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    @Override
    public String getMenuItemLabel() {
        return  i18n.getString("SessionViewMenuItem");
    }

    @Override
    public void requestLoad(CampaignFileInterface items) {
        notesLister.setItems(FXCollections.observableArrayList(items.getNotes().stream().filter(note -> note.getType().endsWith("session")).map(note -> ((SessionNote) note)).toList()));

        String lastLoadedNote = FileAccessLayer.getInstance().getSetting("lastNote");
        if (lastLoadedNote != null) {
            FileAccessLayer.getInstance().getAllNotes().stream().filter(sn -> sn.getReference().toString().equals(lastLoadedNote)).filter(sn -> sn instanceof SessionNote).findFirst().ifPresent(note -> {
                activeNote = (SessionNote) note;
                notesLister.getSelectionModel().select(activeNote);
            });
        }
    }

    @Override
    public void requestSave() {

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
            } catch (IOException e) {}
        });
    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {

    }

    @Override
    public String defineViewerHandlerPrefix() {
        return "session";
    }

    @SuppressWarnings("unused")
    private Callback<UUID, Note> requester;

    @FXML
    private ListView<SessionNote> notesLister;

    private ObservableList<SessionNote> notes;

    private SessionNote activeNote;

    @FXML
    private ScrollPane scroller;

    int dragPosition;

    SessionNote dragElement;

    @FXML
    public void initialize() {

        notes = FXCollections.observableArrayList(new ArrayList<>());
        notesLister.setItems(notes);

        notesLister.setCellFactory(listView -> {
            ListCell<SessionNote> t = new SessionNotesRenderer();

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
                    notesLister.setItems(FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().endsWith("session")).map(note -> (SessionNote) note).toList()));
                }
                e.setDropCompleted(true);
                e.consume();
            });


            t.onDragDetectedProperty().set(e -> {
                dragElement = (SessionNote) t.getItem();
                dragPosition = t.getIndex();
                Dragboard db = t.startDragAndDrop(TransferMode.ANY);
                ClipboardContent c = new ClipboardContent();
                c.putString("accepted");
                db.setContent(c);

                e.consume();
            });


            return t;
        });

        notesLister.getSelectionModel().selectedItemProperty().addListener((observableValue, oldNote, newNote) -> {
            if (newNote != null && oldNote != newNote) {
                activeNote = newNote;
                FileAccessLayer.getInstance().updateSetting("lastNote", newNote.getReference().toString());

                Registry.getInstance().getEditorByFullName("session/session").ifPresent(editorPlugin -> {
                    editorPlugin.prepareToolbar(toolBar, null);
                    Node editor = editorPlugin.defineEditor();

                    scroller.setContent(editor);
                    scroller.setFitToWidth(true);
                    editorPlugin.setOnNoteLoadRequest(this.onNoteLoadRequest);
                    editorPlugin.defineLoadCallback().call(newNote);
                });
            }
        });
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void newSession(ActionEvent actionEvent) {
        SessionNote newNote = new SessionNote();
        newNote.setLabel(i18n.getString("UntitledSession"));
        FileAccessLayer.getInstance().addNote(0, newNote);
        notes.setAll(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().endsWith("session")).map(note -> (SessionNote) note).toList());
        notesLister.setItems(notes);
    }

    public void deleteSession(ActionEvent actionEvent) {
        if (activeNote != null && !activeNote.getContentAsObject().getPlayed() && notes.contains(activeNote)) {
            notes.remove(activeNote);
            activeNote = null;
            notesLister.getSelectionModel().clearSelection();
        }
    }

    public void startSession(ActionEvent actionEvent) {
        //TODO: only enable when a session is selected and was not played yet
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerModule(this);
    }
}
