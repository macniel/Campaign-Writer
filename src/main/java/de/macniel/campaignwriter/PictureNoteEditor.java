package de.macniel.campaignwriter;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

public class PictureNoteEditor implements EditorPlugin {
    @Override
    public NoteType defineHandler() {
        return NoteType.PICTURE_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t) {
        t.getItems().clear();
        Button zoomButton = new Button("", new FontIcon("icm-zoom-in"));
        Button zoomOutButton = new Button("", new FontIcon("icm-zoom-out"));
        Button loadButton = new Button("", new FontIcon("icm-image"));
        Button popoutButton = new Button("", new FontIcon("icm-new-tab"));
        t.getItems().add(zoomButton);
        t.getItems().add(zoomOutButton);
        t.getItems().add(new Separator());
        t.getItems().add(loadButton);
        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10, 1);
        t.getItems().add(spacer);

        t.getItems().add(popoutButton);
        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        return new HBox();
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                return true;
            }
        };
    }

    @Override
    public void loadContent(Note note) {

    }

    @Override
    public void saveContent(Note note) {

    }
}
