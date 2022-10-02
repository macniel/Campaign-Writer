package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.Registry;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.types.ActorNote;
import de.macniel.campaignwriter.types.SessionNote;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;

public class SessionEditor extends EditorPlugin<SessionNote> {

    SessionNote actualNote;
    private ComboBox<Note> sectionProp;
    private VBox scroll;

    @Override
    public String defineHandler() {
        return "session/session";
    }

    @Override
    public void prepareToolbar(Node t, Window w) {
        ToolBar toolBar = (ToolBar) t;
        toolBar.getItems().clear();

        Label addSection = new Label("Neuer Inhalt");

        sectionProp = new ComboBox<>();
        sectionProp.setCellFactory(view -> new NotesRenderer());
        populateSectionProp();

        sectionProp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && oldValue != newValue) {
                actualNote.getContentAsObject().getNotes().add(newValue.getReference());
                updateView();
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

    void populateSectionProp() {
        List<Note> notes = FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> true).toList();
        sectionProp.setItems(FXCollections.observableArrayList(notes));
        sectionProp.getSelectionModel().select(null);
    }

    void updateView() {

        actualNote.getContentAsObject().getNotes().forEach(uuid -> {
            FileAccessLayer.getInstance().findByReference(uuid).ifPresent(note -> {

                Registry.getInstance().getViewerBySuffix(note.getType()).ifPresentOrElse(viewer -> {

                    VBox content = new VBox();
                    Node preview = viewer.getPreviewVersionOf(note);
                    ButtonBar controls = new ButtonBar();
                    Button openFullNote = new Button("open");
                    Button popoutNote = new Button("", new FontIcon("icm-share"));
                    controls.getButtons().addAll(openFullNote, popoutNote);
                    content.getChildren().addAll(preview, controls);
                    scroll.getChildren().add(preview);

                }, () -> {
                    scroll.getChildren().add(new Label("No renderer found for " + note.getLabel() + "[" + uuid.toString() +"]"));

                });

            });
        });
    }

    @Override
    public Note createNewNote() {
        return new SessionNote();
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("session", SessionNote.class);

    }
}
