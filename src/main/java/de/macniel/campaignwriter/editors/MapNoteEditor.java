package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;

public class MapNoteEditor implements EditorPlugin {
    private Callback<String, Note> onNoteRequest;
    private Callback<String, Boolean> onNoteLoadRequest;

    private ScrollPane viewer;

    private ImageView backgroundLayer;

    private Gson gsonParser;

    private MapNoteDefinition noteStructure;
    private Pane root;

    @Override
    public NoteType defineHandler() {
        return NoteType.MAP_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
        t.getItems().clear();
        Button zoomButton = new Button("", new FontIcon("icm-zoom-in"));
        Button zoomOutButton = new Button("", new FontIcon("icm-zoom-out"));

        Button loadButton = new Button("", new FontIcon("icm-image"));
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File actualFile = dialog.showOpenDialog(w);
                if (noteStructure == null) {
                    noteStructure = new MapNoteDefinition();
                }
                noteStructure.backgroundPath = actualFile.getAbsolutePath();
                noteStructure.zoomFactor = 1;
                refreshView();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });
        t.getItems().add(zoomButton);
        t.getItems().add(zoomOutButton);
        t.getItems().add(new Separator());
        t.getItems().add(loadButton);

        t.setVisible(true);
    }

    void refreshView() {
        if (noteStructure != null) {
            root.getChildren().clear();
            root.getChildren().add(backgroundLayer);
            backgroundLayer.imageProperty().set(new Image(noteStructure.backgroundPath));
            viewer.setScaleZ(noteStructure.zoomFactor);

            if (noteStructure.pins == null) {
                noteStructure.pins = new ArrayList<>();
            }
            noteStructure.pins.forEach(this::renderPin);
        }
    }

    @Override
    public Node defineEditor() {
        viewer = new ScrollPane();
        root = new Pane();
        backgroundLayer = new ImageView();
        root.getChildren().add(backgroundLayer);
        backgroundLayer.setX(0);
        backgroundLayer.setY(0);
        viewer.setContent(root);

        root.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                MapPin n = new MapPin();
                n.x = e.getX();
                n.y = e.getY();
                n.label = "Neuer Pin";
                n.noteReference = null;
                noteStructure.pins.add(n);
                renderPin(n);
            }
        });

        return viewer;
    }

    void renderPin(MapPin pin) {
        double pinSize = 32 * noteStructure.zoomFactor;
        Button pinButton = new Button("", new FontIcon("icm-location:32:RED"));
        pinButton.setBackground(Background.EMPTY);
        pinButton.setBorder(Border.EMPTY);
        pinButton.setLayoutX(pin.x - (pinSize/2));
        pinButton.setLayoutY(pin.y - (pinSize/2));
        pinButton.setPrefWidth(pinSize);
        pinButton.setPrefHeight(pinSize);
        Tooltip p = new Tooltip();
        p.setText(pin.label);
        pinButton.setTooltip(p);
        root.getChildren().add(pinButton);
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (gsonParser == null) {
                    gsonParser = new Gson();
                }
                note.setContent(gsonParser.toJson(noteStructure, MapNoteDefinition.class));
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note param) {
                if (gsonParser == null) {
                    gsonParser = new Gson();
                }
                noteStructure = gsonParser.fromJson(param.getContent(), MapNoteDefinition.class);
                refreshView();
                return true;
            }
        };
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {
        this.onNoteRequest = stringNoteCallback;
    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {
        this.onNoteLoadRequest = stringBooleanCallback;
    }
}
