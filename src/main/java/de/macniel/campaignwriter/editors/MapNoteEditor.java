package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.adapters.ColorAdapter;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MapNoteEditor implements EditorPlugin<MapNoteDefinition> {
    private Callback<String, Note> onNoteRequest;
    private Callback<String, Boolean> onNoteLoadRequest;

    private ScrollPane viewer;

    private ImageView backgroundLayer;

    private Gson gsonParser;

    private MapPin selectedPin;

    private MapNoteDefinition noteStructure;
    private Pane root;
    private PropertySheet mapProperties;
    private TextField labelProp;
    private ColorPicker colorProp;
    private ComboBox<Note> noteReferenceProp;

    private Button deletePinButton;
    private VBox mapPropertiesPane;
    private Mode mode;
    private Line rulerLine;

    private double rulerStartX;
    private double rulerStartY;

    private boolean dragging;
    private double rulerEndX;

    private double rulerEndY;

    private enum Mode {
        POINTER,
        MEASURE,
        SCALE
    }

    @Override
    public NoteType defineHandler() {
        return NoteType.MAP_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
        t.getItems().clear();
        Button zoomButton = new Button("", new FontIcon("icm-zoom-in"));
        Button zoomOutButton = new Button("", new FontIcon("icm-zoom-out"));

        ToggleGroup modeGroup = new ToggleGroup();

        ToggleButton scaleModeButton = new ToggleButton("S");
        ToggleButton pointerModeButton = new ToggleButton("P");

        Button loadButton = new Button("", new FontIcon("icm-image"));
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File actualFile = dialog.showOpenDialog(w);
                if (noteStructure == null) {
                    noteStructure = new MapNoteDefinition();
                }
                FileAccessLayer.getInstance().getImageFromString(actualFile.getAbsolutePath()).ifPresent(entry -> {
                    noteStructure.backgroundPath = entry.getKey();
                    System.out.println("storing image as " + entry.getKey());
                    noteStructure.zoomFactor = 1;
                    refreshView();
                });
                
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        pointerModeButton.setToggleGroup(modeGroup);

        scaleModeButton.setToggleGroup(modeGroup);

        modeGroup.selectedToggleProperty().addListener( (observable, oldValue, newValue) -> {
            if (pointerModeButton.equals(newValue)) {
                this.mode = Mode.POINTER;
            } else if (scaleModeButton.equals(newValue)) {
                this.mode = Mode.SCALE;
            }
        });

        t.getItems().add(zoomButton);
        t.getItems().add(zoomOutButton);
        t.getItems().add(new Separator());
        t.getItems().add(pointerModeButton);
        t.getItems().add(scaleModeButton);
        t.getItems().add(new Separator());
        t.getItems().add(loadButton);

        t.setVisible(true);
    }

    void refreshDragAndDropHandler() {

        viewer.setOnDragDetected(e -> {
            viewer.startDragAndDrop(TransferMode.LINK);
            e.consume();
        });

        viewer.setOnDragDropped(e -> {
            if (e.getDragboard().hasFiles()) {
                if (noteStructure == null) {
                    noteStructure = new MapNoteDefinition();
                }
                noteStructure.backgroundPath = e.getDragboard().getFiles().get(0).getAbsolutePath();
                refreshView();
            }
        });

        viewer.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                /* allow for moving */
                e.acceptTransferModes(TransferMode.LINK);
            }
            e.consume();
        });

    }

    void refreshView() {
        if (noteStructure != null) {
            root.getChildren().clear();
            root.getChildren().add(backgroundLayer);
            FileAccessLayer.getInstance().getImageFromString(noteStructure.backgroundPath).ifPresent(entry -> {
                backgroundLayer.imageProperty().set(entry.getValue());
                viewer.setScaleZ(noteStructure.zoomFactor);    
            });
           
            BorderPane bp = (BorderPane) viewer.getParent();
            viewer.setContent(null);
            bp.setCenter(null);
            viewer = new ScrollPane();
            viewer.setContent(root);

            viewer.onScrollProperty().set(scrollEvent -> {

                noteStructure.scrollPositionX += scrollEvent.getDeltaX();
                noteStructure.scrollPositionY += scrollEvent.getDeltaY();
                System.out.print(viewer.getHvalue());
                System.out.println(viewer.getVvalue());
            });
            viewer.setPannable(true);

            viewer.setHvalue(noteStructure.scrollPositionX);
            viewer.setVvalue(noteStructure.scrollPositionY);

            bp.setCenter(viewer);
            refreshDragAndDropHandler();

            if (noteStructure.pins == null) {
                noteStructure.pins = new ArrayList<>();
            }
            noteStructure.pins.forEach(this::renderPin);
        }
    }

    double getDistance(double sX, double sY, double eX, double eY) {
        return Math.sqrt(
                Math.pow(( Math.max(sX, eX) - Math.min(eX, sX) ), 2) +
                        Math.pow(( Math.max(sY, eY) - Math.min(eY, sY) ), 2));

    }

    @Override
    public Node defineEditor() {
        BorderPane bp = new BorderPane();
        viewer = new ScrollPane();
        root = new Pane();
        backgroundLayer = new ImageView();
        root.getChildren().add(backgroundLayer);
        backgroundLayer.setX(0);
        backgroundLayer.setY(0);
        viewer.setContent(root);

        root.setOnMouseMoved(e -> {
            if (dragging) {
                dragging = false;

                TextInputDialog measuredLength = new TextInputDialog();
                measuredLength.setTitle("Längen Bemessung");

                double distance = getDistance(rulerStartX, rulerStartY, rulerEndX, rulerEndY);

                measuredLength.setHeaderText("Gemessen wurden " + distance + " Pixel" + " und entsprechen wieviel Meter?");
                Optional<String> result = measuredLength.showAndWait();
                if (result.isPresent()) {

                    // Skalierungsfaktor = 1 zu X
                    double uniformedFactor = Double.valueOf(result.get());

                    noteStructure.scale = uniformedFactor / distance;
                    root.getChildren().remove(rulerLine);
                    rulerLine = null;
                }

            }
        });

        root.setOnMouseDragged(e -> {
            switch (mode) {
                case SCALE -> {
                    System.out.println(dragging + " from " + rulerStartX + "by" + rulerStartY);
                    if (!dragging) {
                        rulerStartX = e.getX();
                        rulerStartY = e.getY();
                        if (rulerLine != null) {
                            root.getChildren().remove(rulerLine);
                        }
                        rulerLine = new Line();
                        root.getChildren().add(rulerLine);
                        rulerLine.setStartX(rulerStartX);
                        rulerLine.setStartY(rulerStartY);
                        rulerLine.setStrokeLineCap(StrokeLineCap.ROUND);
                        rulerLine.setStrokeWidth(2);
                        rulerLine.setStroke(Color.RED);

                        dragging = true;
                    } else {
                        if (rulerLine != null) {
                            rulerEndX = e.getX();
                            rulerEndY = e.getY();
                            rulerLine.setEndX(e.getX());
                            rulerLine.setEndY(e.getY());
                            if (noteStructure.scale != 0) {
                                double pixelDistance = getDistance(rulerStartX, rulerStartY, rulerEndX, rulerEndY);
                                System.out.println("Distanz " + pixelDistance * noteStructure.scale + " p");
                            }
                        }
                    }
                }
            }
        });

        root.setOnMouseClicked(e -> {
            switch (mode) {
                case POINTER -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        MapPin n = new MapPin();
                        n.x = e.getX();
                        n.y = e.getY();
                        n.label = "Neuer Pin";
                        n.noteReference = null;
                        noteStructure.pins.add(n);
                        selectedPin = n;
                        renderPin(n);
                        updateEditor();
                    } else {
                        selectedPin = null;
                        mapPropertiesPane.setVisible(false);
                    }
                }
                case SCALE -> {}
            }
        });

        mapPropertiesPane = new VBox();

        labelProp = new TextField();
        colorProp = new ColorPicker();
        noteReferenceProp = new ComboBox<Note>();
        deletePinButton = new Button("Pin entfernen");


        mapPropertiesPane.getChildren().add(new Label("Beschriftung"));
        mapPropertiesPane.getChildren().add(labelProp);

        labelProp.onActionProperty().set(e -> {
            if (selectedPin != null) {
                selectedPin.label = labelProp.getText();
                refreshView();
            }

        });

        mapPropertiesPane.getChildren().add(new Label("Farbe"));
        mapPropertiesPane.getChildren().add(colorProp);

        colorProp.onActionProperty().set(e -> {
            if (selectedPin != null) {
                selectedPin.color = colorProp.getValue();
                refreshView();
            }
        });

        mapPropertiesPane.getChildren().add(new Label("Verknüpfte Notiz"));
        mapPropertiesPane.getChildren().add(noteReferenceProp);

        noteReferenceProp.onActionProperty().set(e -> {
            Note selected = noteReferenceProp.getValue();
            if (selectedPin != null && selected != null) {
                selectedPin.noteReference = selected.reference;
                refreshView();
            }
        });


        mapPropertiesPane.getChildren().add(new Separator());

        mapPropertiesPane.getChildren().add(deletePinButton);

        deletePinButton.onActionProperty().set(e -> {
            if (selectedPin != null) {
                noteStructure.pins.remove(selectedPin);
                selectedPin = null;
                refreshView();
                mapPropertiesPane.setVisible(false);
            }
        });

        bp.setRight(mapPropertiesPane);
        bp.setCenter(viewer);

        populateNoteReferenceProp();
        refreshDragAndDropHandler();
        this.mode = Mode.POINTER;

        return bp;
    }

    void populateNoteReferenceProp() {
        List<Note> notes = FileAccessLayer.getInstance().getAllNotes(); // TODO: this should not be possible, request maincontroller instead
        noteReferenceProp.setItems(FXCollections.observableArrayList(notes));

        noteReferenceProp.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(ListView<Note> noteListView) {
                return new NotesRenderer();
            }
        });

    }

    void updateEditor() {
        populateNoteReferenceProp();

        labelProp.setText(selectedPin.label);

        if (selectedPin.noteReference != null) {
            noteReferenceProp.getSelectionModel().select(onNoteRequest.call(selectedPin.noteReference.toString()));
        }
        mapPropertiesPane.setVisible(true);
    }

    void renderPin(MapPin pin) {
        double pinSize = 32 * noteStructure.zoomFactor;
        if (pin.color == null) {
            pin.color = Color.RED;
        }
        Button pinButton = new Button("", new FontIcon("icm-location:32:" + pin.color.toString()));
        pinButton.setBackground(Background.EMPTY);
        pinButton.setBorder(Border.EMPTY);
        pinButton.setLayoutX(pin.x - (pinSize/2));
        pinButton.setLayoutY(pin.y - (pinSize/2));
        pinButton.setPrefWidth(pinSize);
        pinButton.setPrefHeight(pinSize);
        pinButton.setCursor(Cursor.HAND);

        Tooltip p = new Tooltip();
        p.setText(pin.label);


        pinButton.onMouseClickedProperty().set(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                selectedPin = pin;
                updateEditor();
                System.out.println("Pin rightclicked");
            } else {
                if (pin.noteReference != null) {
                    onNoteLoadRequest.call(pin.noteReference.toString());
                }
                mapPropertiesPane.setVisible(false);
            }
        });
        pinButton.setTooltip(p);
        root.getChildren().add(pinButton);
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (gsonParser == null) {
                    gsonParser = new GsonBuilder().registerTypeAdapter(Color.class, new ColorAdapter()).create();
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
                    gsonParser = new GsonBuilder().registerTypeAdapter(Color.class, new ColorAdapter()).create();
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

    @Override
    public Node getPreviewVersionOf(MapNoteDefinition t) {
        return new VBox();
    }
}
