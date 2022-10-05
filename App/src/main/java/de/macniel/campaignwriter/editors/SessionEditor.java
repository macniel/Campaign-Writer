package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.Registry;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.types.SessionNote;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class SessionEditor extends EditorPlugin<SessionNote> {

    private final ResourceBundle i18n;
    SessionNote actualNote;
    private ComboBox<Note> sectionProp;
    private VBox scroll;
    private Callback<String, Boolean> requester;


    public static String getLocalizationBase() {
        return "i18n.sessions";
    }

    public SessionEditor() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
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
        sectionProp.setCellFactory(view -> new NotesRenderer());
        populateSectionProp();

        sectionProp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != newValue) {
                actualNote.getContentAsObject().getNotes().add(newValue.getReference());
                sectionProp.getSelectionModel().select(null);
                updateView();
            }
        });

        toolBar.getItems().addAll(addSection, sectionProp);

        toolBar.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        scroll = new VBox();
        HBox.setHgrow(scroll, Priority.ALWAYS);
        scroll.setFillWidth(true);
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

    void populateSectionProp() {
        List<Note> notes = new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> !note.getType().equals("session")).filter(note -> Registry.getInstance().getViewerBySuffix(note.getType()).isPresent()).toList();
        sectionProp.setItems(FXCollections.observableArrayList(notes));
        sectionProp.getSelectionModel().select(null);
    }

    void updateView() {

        scroll.getChildren().clear();

        actualNote.getContentAsObject().getNotes().forEach(uuid -> {
            new FileAccessLayerFactory().get().findByReference(uuid).ifPresent(note -> {

                Registry.getInstance().getViewerBySuffix(note.getType()).ifPresentOrElse(viewer -> {

                    VBox content = new VBox();
                    VBox.setMargin(content, new Insets(10));

                    content.getStyleClass().add("scroll-item");
                    content.setMinWidth(500);
                    content.setFillWidth(true);
                    Node preview = viewer.getPreviewVersionOf(note);
                    if (preview == null) {
                        throw new NullPointerException();
                    }
                    VBox.setMargin(preview, new Insets(5, 20, 5, 20));
                    HBox controls = new HBox();
                    HBox.setMargin(preview, new Insets(0, 20, 5, 20));

                    Button deleteNote = new Button(i18n.getString("DeleteNote"));
                    Button openFullNote = new Button(i18n.getString("OpenNote"));
                    FontIcon shareIcon =new FontIcon("icm-share");
                    shareIcon.setIconColor(Color.BLUE);
                    Button popoutNote = new Button(i18n.getString("PopoutNote"), shareIcon);

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
                            requester.call(uuid.toString());
                        }
                    });

                    popoutNote.onActionProperty().set(e -> {
                        Stage popout = new Stage();



                        Node standalone = viewer.getStandaloneVersion(note, null);

                        popout.setScene(new Scene(new VBox(standalone)));

                        popout.setWidth(400);
                        popout.setHeight(300);
                        popout.setTitle(note.getLabel());
                        popout.showAndWait();

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
                    scroll.getChildren().add(new Label("No renderer found for " + note.getLabel() + "[" + uuid.toString() +"] of type " + note.getType()));

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
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("session", SessionNote.class);

    }
}
