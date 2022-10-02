package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import de.macniel.campaignwriter.types.Picture;
import de.macniel.campaignwriter.types.PictureNote;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
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

public class PictureNoteEditor extends EditorPlugin<PictureNote> implements ViewerPlugin<PictureNote> {
    private ImageView viewer;

    private PictureNote actualNote;

    @Override
    public String defineHandler() {
        return "building/picture";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {

        ToolBar t = (ToolBar) n;

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
            actualNote.getContentAsObject().setZoomFactor(actualNote.getContentAsObject().getZoomFactor() + 0.25);
            updateView();
        });

        zoomOutButton.onActionProperty().set(e -> {
            actualNote.getContentAsObject().setZoomFactor(actualNote.getContentAsObject().getZoomFactor() - 0.25);
            updateView();
        });
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File file = dialog.showOpenDialog(w);
                FileAccessLayer.getInstance().getImageFromString(file.getAbsolutePath()).ifPresent(entry -> {
                    actualNote.getContentAsObject().setFileName(entry.getKey());
                    actualNote.getContentAsObject().setZoomFactor(1);
                });
                updateView();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        popoutButton.onActionProperty().set(e -> {
            if (actualNote != null) {
                Rectangle2D rect = Screen.getPrimary().getBounds();
                Stage s = new Stage();
                ScrollPane parentNode = new ScrollPane();

                ImageView popoutViewer = new ImageView();
                FileAccessLayer.getInstance().getImageFromString(actualNote.getContentAsObject().getFileName()).ifPresent(entry -> {
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
        ScrollPane p = new ScrollPane(this.viewer);
        p.setPannable(true);
        p.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        p.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        return p;
    }

    @Override
    public Callback<Boolean, PictureNote> defineSaveCallback() {
        return note -> actualNote;
    }

    private void updateView() {
        if (actualNote != null) {
            FileAccessLayer.getInstance().getImageFromString(actualNote.getContentAsObject().getFileName()).ifPresent(entry -> {
                viewer.imageProperty().set(entry.getValue());
                viewer.setFitHeight(actualNote.getContentAsObject().getZoomFactor() * viewer.imageProperty().get().getHeight());
                viewer.setFitWidth(actualNote.getContentAsObject().getZoomFactor() * viewer.imageProperty().get().getWidth());
            });
        }
    }

    @Override
    public Callback<PictureNote, Boolean> defineLoadCallback() {
        return note -> {
            actualNote = note;
            updateView();
            return true;
        };
    }

    @Override
    public Node getPreviewVersionOf(PictureNote t) {
        return new VBox();
    }

    @Override
    public Node getStandaloneVersion(PictureNote t, Stage wnd) {
        return getPreviewVersionOf(t);
    }

    @Override
    public Note createNewNote() {
        return new PictureNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("picture", PictureNote.class);
    }


    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}
