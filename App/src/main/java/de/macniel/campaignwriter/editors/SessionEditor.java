package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.Registry;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.SessionNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.*;

public class SessionEditor extends EditorPlugin<SessionNote> {

    private final ResourceBundle i18n;
    SessionNote actualNote;
    Callback<UUID, Boolean> openNoteInStandaloneViewCallback = param -> {
        Optional<Note> requestedNote = new FileAccessLayerFactory().get().findByReference(param);
        if (requestedNote.isPresent()) {
            Registry.getInstance().getViewerFor(requestedNote.get()).ifPresent(subViewer -> {
                openInStandaloneView(requestedNote.get(), subViewer);
            });
            return true;
        } else {
            return false;
        }
    };
    private ComboBox<Note> sectionProp;
    private VBox scroll;
    private Callback<UUID, Boolean> requester;

    public SessionEditor() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    public static String getLocalizationBase() {
        return "i18n.sessions";
    }

    @Override
    public String defineHandler() {
        return "session/session";
    }

    @Override
    public void prepareToolbar(Node t, Window w) {
        ToolBar toolBar = (ToolBar) t;
        toolBar.getItems().clear();

        Label addSection = new Label(i18n.getString("AddSection"));

        sectionProp = new ComboBox<>();
        sectionProp.setCellFactory(view -> new NotesRenderer(true));
        sectionProp.setButtonCell(new NotesRenderer(true));
        populateSectionProp();

        sectionProp.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != newValue) {
                actualNote.getContentAsObject().getNotes().add(newValue.getReference());
                System.out.println("here?");
                updateView();
                // FIXME: needs to be cleared
            }
        });

        toolBar.getItems().addAll(addSection, sectionProp);

        toolBar.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        scroll = new VBox();
        return scroll;
    }

    @Override
    public Callback<Boolean, SessionNote> defineSaveCallback() {
        return p -> actualNote;
    }

    @Override
    public Callback<SessionNote, Boolean> defineLoadCallback() {
        return note -> {
            actualNote = note;
            updateView();
            return true;
        };
    }

    ObservableList<Note> populateSectionProp() {
        List<Note> notes = new ArrayList<>(new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> !note.getType().equals("session")).filter(note -> Registry.getInstance().getViewerFor(note).isPresent()).toList());
        ObservableList<Note> list = FXCollections.observableArrayList(notes);

        sectionProp.setItems(list);
        //sectionProp.getSelectionModel().select(null);
        return list;
    }

    void openInStandaloneView(Note note, ViewerPlugin withViewer) {
        Stage popout = new Stage();


        Node standalone = withViewer.getStandaloneVersion(note, null);
        BorderPane p = new BorderPane();
        if (standalone == null) {
            p.setCenter(new Label("Error in displaying standalone version for note " + note.getReference() + " as type " + note.getType()));
        } else {
            p.setCenter(standalone);
            VBox.setVgrow(p, Priority.ALWAYS);
            HBox.setHgrow(p, Priority.ALWAYS);
        }

        popout.setScene(new Scene(p, 400, 300));

        popout.setWidth(400);
        popout.setHeight(300);
        popout.setTitle(note.getLabel());
        popout.showAndWait();
    }

    void updateView() {

        scroll.getChildren().clear();
        System.out.println(scroll.getWidth());

        actualNote.getContentAsObject().getNotes().forEach(uuid -> {
            new FileAccessLayerFactory().get().findByReference(uuid).ifPresent(note -> {
                Registry.getInstance().getViewerFor(note).ifPresentOrElse(viewer -> {

                    VBox content = new VBox();
                    VBox.setMargin(content, new Insets(10));

                    content.getStyleClass().add("scroll-item");
                    content.setMinWidth(500);
                    //content.setFillWidth(true);
                    Node preview = viewer.getPreviewVersionOf(note);
                    if (preview == null) {
                        preview = new Text("Error in rendering preview of note " + note.getReference() + " of type " + note.getType());
                    }
                    VBox.setMargin(preview, new Insets(5, 20, 5, 20));
                    HBox controls = new HBox();
                    HBox.setMargin(preview, new Insets(0, 20, 5, 20));

                    Button deleteNote = new Button(i18n.getString("DeleteNote"));
                    Button openFullNote = new Button(i18n.getString("OpenNote"));
                    FontIcon shareIcon = new FontIcon("icm-share");
                    shareIcon.setIconColor(Color.BLUE);
                    Button popoutNote = new Button(i18n.getString("PopoutNote"), shareIcon);

                    // Intercept notesrequester to open in a new standalone wnd
                    viewer.setOnNoteLoadRequest(openNoteInStandaloneViewCallback);

                    deleteNote.onActionProperty().set(e -> {
                        try {
                            actualNote.getContentAsObject().getNotes().remove(uuid);
                            new FileAccessLayerFactory().get().saveToFile();
                            updateView();
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    openFullNote.onActionProperty().set(e -> {
                        if (requester != null) {
                            requester.call(uuid);
                        }
                    });

                    popoutNote.onActionProperty().set(e -> {
                        openInStandaloneView(note, viewer);
                    });

                    HBox spacer = new HBox();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    openFullNote.getStyleClass().add("scroll-link");
                    popoutNote.getStyleClass().add("scroll-button");

                    controls.getChildren().addAll(deleteNote, spacer, openFullNote, popoutNote);
                    content.getChildren().addAll(preview, controls);
                    HBox.setHgrow(content, Priority.ALWAYS);
                    scroll.getChildren().add(content);
                }, () -> {
                    scroll.getChildren().add(new Label("No renderer found for " + note.getLabel() + "[" + uuid.toString() + "] of type " + note.getType()));

                });

            });
        });
        HBox.setHgrow(scroll, Priority.ALWAYS);
        scroll.setBorder(Border.EMPTY);
    }

    @Override
    public Note createNewNote() {
        return new SessionNote();
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("session", SessionNote.class);

    }
}
