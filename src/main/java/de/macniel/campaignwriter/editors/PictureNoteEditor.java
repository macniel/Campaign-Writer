package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
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
import com.google.gson.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class PictureNoteEditor implements EditorPlugin {
    private ImageView viewer;

    private Gson gsonParser;

    private PictureNoteDefinition noteStructure;

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
            noteStructure.zoomFactor += 0.25;
            refreshImageView();
        });

        zoomOutButton.onActionProperty().set(e -> {
            noteStructure.zoomFactor -= 0.25;
            refreshImageView();
        });
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File actualFile = dialog.showOpenDialog(w);
                if (noteStructure == null) {
                    noteStructure = new PictureNoteDefinition();
                }
                noteStructure.fileName = actualFile.getAbsolutePath();
                noteStructure.zoomFactor = 1;
                refreshImageView();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        this.viewer = new ImageView();
        this.viewer.preserveRatioProperty().set(true);
        this.gsonParser = new Gson();
        this.noteStructure = new PictureNoteDefinition();
        this.noteStructure.zoomFactor = 1;
        return this.viewer;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                note.setContent(gsonParser.toJson(noteStructure));
                return true;
            }
        };
    }

    private void refreshImageView() {
        if (noteStructure != null) {
            viewer.imageProperty().set(new Image(noteStructure.fileName));
            viewer.setFitHeight(noteStructure.zoomFactor * viewer.imageProperty().get().getHeight());
            viewer.setFitWidth(noteStructure.zoomFactor * viewer.imageProperty().get().getWidth());
        }
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {

                noteStructure = gsonParser.fromJson(note.getContent(), PictureNoteDefinition.class);
                refreshImageView();
                return true;
            }
        };
    }
}
