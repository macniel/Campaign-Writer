package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.editors.SessionNote;
import de.macniel.campaignwriter.viewers.LocationViewer;
import de.macniel.campaignwriter.viewers.MapViewer;
import de.macniel.campaignwriter.viewers.SceneViewer;
import de.macniel.campaignwriter.viewers.TextViewer;
import de.macniel.campaignwriter.viewers.ViewerPlugin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class SessionView extends ViewInterface {

    private final ResourceBundle i18n;
    @SuppressWarnings("unused")
    private Callback<UUID, Note> requester;

    @FXML
    private ListView<SessionNote> notesLister;

    private ObservableList<SessionNote> notes;

    private SessionNote activeNote;

    @FXML
    private VBox scriptBox;

    @FXML
    private ScrollPane scroller;

    int dragPosition;

    SessionNote dragElement;
    private ArrayList<ViewerPlugin> scrollInterpreter;

    @Override
    public String getPathToFxmlDefinition() {
        return "session-view.fxml";
    }

    public static String getLocalizationBase() {
        return "i18n.sessions";
    }

    public SessionView() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    @Override
    public String getMenuItemLabel() {
        return  i18n.getString("SessionViewMenuItem");
    }

    @Override
    public void requestLoad(CampaignFile items) {
        scriptBox.getChildren().clear();

        notesLister.setItems(FXCollections.observableArrayList(items.sessionNotes));

        String lastLoadedNote = FileAccessLayer.getInstance().getSetting("lastNote");
        if (lastLoadedNote != null) {
            FileAccessLayer.getInstance().getAllSessionNotes().stream().filter(sn -> sn.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                activeNote = note;
                notesLister.getSelectionModel().select(activeNote);
            });
        }

    }

    @Override
    public void requestSave() {

    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }

    Callback<UUID, Note> noteRequestRenderer;

    @FXML
    public void initialize() {

        notes = FXCollections.observableArrayList(new ArrayList<>());
        notesLister.setItems(notes);
        scrollInterpreter = new ArrayList<ViewerPlugin>();
        scrollInterpreter.add(new TextViewer());
        scrollInterpreter.add(new MapViewer());
        scrollInterpreter.add(new SceneViewer());
        scrollInterpreter.add(new LocationViewer());

        noteRequestRenderer = new Callback<UUID,Note>() {
            @Override
            public Note call(UUID param) {
                FileAccessLayer.getInstance().findByReference(param).ifPresent(note -> {
                    scrollInterpreter.stream().filter(si -> si.defineNoteType() == note.getType()).findFirst().ifPresent(viewer -> {
                        final Stage wnd = new Stage();
                        AnchorPane b = new AnchorPane();
                        b.getChildren().add(viewer.renderNoteStandalone(note));
                        Scene popoutContent = new Scene(b, 300, 300);
                        wnd.setScene(popoutContent);
                        wnd.show();
                    });
                });
                return null;
            }
        };

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
                    FileAccessLayer.getInstance().removeSessionNote(dragElement);
                    FileAccessLayer.getInstance().addSessionNote(dragPosition, dragElement);
                    notesLister.setItems(FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllSessionNotes()));
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

        notesLister.getSelectionModel().selectedItemProperty().addListener((observableValue, oldNote, newNote) -> {
            activeNote = newNote;
            FileAccessLayer.getInstance().updateSetting("lastNote", newNote.getReference().toString());
            updateScroll();
        });
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void updateScroll() {
        if (activeNote != null) {
            scroller.setVisible(true);
            scriptBox.getChildren().clear();

            activeNote.getNotes().forEach(noteUUID -> {
                FileAccessLayer.getInstance().findByReference(noteUUID).ifPresent(note -> {
                    HBox line = new HBox();
                    AnchorPane p = new AnchorPane();

                    scrollInterpreter
                            .stream()
                            .filter(vp -> vp.defineNoteType() == note.type)
                            .findFirst()
                            .ifPresent(viewerPlugin -> {
                                Node viewNode = viewerPlugin.renderNote(note, scroller.widthProperty(), noteRequestRenderer);
                                p.getChildren().add(viewNode);
                                AnchorPane.setBottomAnchor(viewNode, 0.0);
                                AnchorPane.setTopAnchor(viewNode, 0.0);
                                AnchorPane.setLeftAnchor(viewNode, 0.0);
                                AnchorPane.setRightAnchor(viewNode, 0.0);


                            });


                    line.getChildren().add(p);
                    scriptBox.getChildren().add(line);
                });
            });

            ComboBox<Note> adder = new ComboBox<>();
            List<Note> filteredList = FileAccessLayer.getInstance().getAllNotes().stream().filter(n -> {
                return n.type == NoteType.TEXT_NOTE ||
                        n.type == NoteType.SCENE_NOTE ||
                        n.type == NoteType.MAP_NOTE;
            }).toList();
            adder.setItems(FXCollections.observableArrayList(filteredList));
            adder.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
                @Override
                public ListCell<Note> call(ListView<Note> noteListView) {
                   return new NotesRenderer();
                }
            });

            adder.getSelectionModel().selectedItemProperty().addListener((observableValue, note, toAdd) -> {
                activeNote.getNotes().add(toAdd.reference);
                updateScroll();
            });

            scriptBox.getChildren().add(adder);
        } else {
            scroller.setVisible(false);
        }
    }

    public void newSession(ActionEvent actionEvent) {
        SessionNote newNote = new SessionNote();
        newNote.setLabel(i18n.getString("UntitledSession"));
        FileAccessLayer.getInstance().addSessionNote(0, newNote);
        notes.setAll(FileAccessLayer.getInstance().getAllSessionNotes());
        notesLister.setItems(notes);
    }

    public void deleteSession(ActionEvent actionEvent) {
        if (activeNote != null && !activeNote.getPlayed() && notes.contains(activeNote)) {
            notes.remove(activeNote);
            activeNote = null;
            notesLister.getSelectionModel().clearSelection();
            updateScroll();
        }
    }

    public void startSession(ActionEvent actionEvent) {
        //TODO: only enable when a session is selected and was not played yet
    }
}
