package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.Map;

public class PictureNoteEditor implements EditorPlugin<PictureNoteDefinition> {
    private ImageView viewer;

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
                FileAccessLayer.getInstance().getImageFromString(actualFile.getAbsolutePath()).ifPresent(entry -> {
                    noteStructure.fileName = entry.getKey();
                    noteStructure.zoomFactor = 1;
                });
                refreshImageView();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        popoutButton.onActionProperty().set(e -> {
            if (noteStructure != null) {
                Rectangle2D rect = Screen.getPrimary().getBounds();
                Stage s = new Stage();
                ScrollPane parentNode = new ScrollPane();

                ImageView popoutViewer = new ImageView();
                FileAccessLayer.getInstance().getImageFromString(noteStructure.fileName).ifPresent(entry -> {
                    popoutViewer.imageProperty().set(entry.getValue());
                    double width = Math.min(rect.getWidth(), popoutViewer.imageProperty().get().getWidth());
                    double height = Math.min(rect.getHeight(), popoutViewer.imageProperty().get().getHeight());
    
                    if (width > height) {
                        popoutViewer.setFitHeight(height);
                    } else {
                        popoutViewer.setFitWidth(width);
                    }
                });
                

                parentNode.setFitToHeight(true);
                parentNode.setFitToWidth(true);
                popoutViewer.preserveRatioProperty().set(true);

                parentNode.setContent(popoutViewer);

                parentNode.setMaxHeight(rect.getHeight());
                parentNode.setMaxWidth(rect.getWidth());

                Scene popout = new Scene(parentNode);
                s.setScene(popout);

                s.show();
            }
        });

        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        this.viewer = new ImageView();
        this.viewer.preserveRatioProperty().set(true);
        this.noteStructure = new PictureNoteDefinition();
        this.noteStructure.zoomFactor = 1;
        ScrollPane p = new ScrollPane(this.viewer);
        p.setPannable(true);
        p.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        p.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return p;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return note -> {
            note.setContent(FileAccessLayer.getInstance().getParser().toJson(noteStructure));
            return true;
        };
    }

    private void refreshImageView() {
        if (noteStructure != null) {
            FileAccessLayer.getInstance().getImageFromString(noteStructure.fileName).ifPresent(entry -> {
                viewer.imageProperty().set(entry.getValue());
                viewer.setFitHeight(noteStructure.zoomFactor * viewer.imageProperty().get().getHeight());
                viewer.setFitWidth(noteStructure.zoomFactor * viewer.imageProperty().get().getWidth());
            });
        }
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return note -> {
            noteStructure = FileAccessLayer.getInstance().getParser().fromJson(note.getContent(), PictureNoteDefinition.class);
            refreshImageView();
            return true;
        };
    }


    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }

    @Override
    public Node getPreviewVersionOf(PictureNoteDefinition t) {
        return new VBox();
    }
}
