package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PictureNoteEditor implements EditorPlugin {
    private ImageView viewer;

    private File actualFile;

    private double zoomFactor = 200;

    @Override
    public NoteType defineHandler() {
        return NoteType.PICTURE_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
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

        zoomButton.onActionProperty().set(e -> {
            zoomFactor += 25;
            viewer.setFitHeight(zoomFactor);
            viewer.setFitWidth(zoomFactor);
        });

        zoomOutButton.onActionProperty().set(e -> {
            zoomFactor -= 25;
            viewer.setFitHeight(zoomFactor);
            viewer.setFitWidth(zoomFactor);
        });
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                actualFile = dialog.showOpenDialog(w);
                viewer.imageProperty().set(new Image(new FileInputStream(actualFile)));
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });

        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        this.viewer = new ImageView();
        this.viewer.preserveRatioProperty().set(true);
        zoomFactor = 1000;
        return this.viewer;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (actualFile != null) {
                    note.setContent(actualFile.getAbsolutePath());
                }
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                actualFile = new File(note.getContent());
                viewer.imageProperty().set(new Image(actualFile.getAbsolutePath()));
                return true;
            }
        };
    }
}
