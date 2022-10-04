package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import de.macniel.campaignwriter.types.Map;
import de.macniel.campaignwriter.types.MapNote;
import de.macniel.campaignwriter.types.MapPin;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class MapNoteEditor extends EditorPlugin<MapNote> implements ViewerPlugin<MapNote> {
    private Callback<String, MapNote> onNoteRequest;
    private Callback<String, Boolean> onNoteLoadRequest;

    private ScrollPane viewer;

    private ImageView backgroundLayer;

    private MapPin selectedPin;

    private MapNote actualNote;
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
    private ResourceBundle i18n;

    private enum Mode {
        POINTER,
        MEASURE,
        FOG, SCALE
    }

    public MapNoteEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

    @Override
    public String defineHandler() {
        return "building/map";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();
        Button zoomButton = new Button("", new FontIcon("icm-zoom-in"));
        Button zoomOutButton = new Button("", new FontIcon("icm-zoom-out"));

        ToggleGroup modeGroup = new ToggleGroup();

        ToggleButton scaleModeButton = new ToggleButton("S");
        ToggleButton pointerModeButton = new ToggleButton("P");
        ToggleButton fogModeButton = new ToggleButton("F");

        Button loadButton = new Button("", new FontIcon("icm-image"));
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File file = dialog.showOpenDialog(w);
                FileAccessLayer.getInstance().getImageFromString(file.getAbsolutePath()).ifPresent(entry -> {
                    actualNote.getContentAsObject().backgroundPath = entry.getKey();
                    actualNote.getContentAsObject().setZoomFactor(1);
                    updateView();
                });
                
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        pointerModeButton.setToggleGroup(modeGroup);

        scaleModeButton.setToggleGroup(modeGroup);

        fogModeButton.setToggleGroup(modeGroup);

        modeGroup.selectedToggleProperty().addListener( (observable, oldValue, newValue) -> {
            if (pointerModeButton.equals(newValue)) {
                this.mode = Mode.POINTER;
            } else if (scaleModeButton.equals(newValue)) {
                this.mode = Mode.SCALE;
            } else if (fogModeButton.equals(newValue)) {
                this.mode = Mode.FOG;
            }
        });

        t.getItems().add(zoomButton);
        t.getItems().add(zoomOutButton);
        t.getItems().add(new Separator());
        t.getItems().add(fogModeButton);
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
                actualNote.getContentAsObject().backgroundPath = e.getDragboard().getFiles().get(0).getAbsolutePath();
                updateView();
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

    void updateView() {
        if (actualNote != null) {
            root.getChildren().clear();
            root.getChildren().add(backgroundLayer);
            FileAccessLayer.getInstance().getImageFromString(actualNote.getContentAsObject().backgroundPath).ifPresent(entry -> {
                WritableImage image = new WritableImage(entry.getValue().getPixelReader(), (int)entry.getValue().getWidth(), (int)entry.getValue().getHeight());
                if (actualNote.getContentAsObject().getFog() == null) {
                    actualNote.getContentAsObject().setFog(new ArrayList<>());
                }
                actualNote.getContentAsObject().getFog().forEach(point -> {

                    int fogColor = 50 << 24;

                    image.getPixelWriter().setArgb((int)point.getX(), (int)point.getY(), fogColor);

                });

                backgroundLayer.imageProperty().set(image);
                viewer.setScaleZ(actualNote.getContentAsObject().getZoomFactor());
            });


           
            BorderPane bp = (BorderPane) viewer.getParent();
            viewer.setContent(null);
            bp.setCenter(null);
            viewer = new ScrollPane();
            viewer.setContent(root);

            viewer.onScrollProperty().set(scrollEvent -> {

                actualNote.getContentAsObject().setScrollPositionX(actualNote.getContentAsObject().getScrollPositionX() + scrollEvent.getDeltaX());
                actualNote.getContentAsObject().setScrollPositionY(actualNote.getContentAsObject().getScrollPositionY() + scrollEvent.getDeltaY());
            });
            viewer.setPannable(true);

            viewer.setHvalue(actualNote.getContentAsObject().getScrollPositionX());
            viewer.setVvalue(actualNote.getContentAsObject().getScrollPositionY());

            bp.setCenter(viewer);
            refreshDragAndDropHandler();

            if (actualNote.getContentAsObject().getPins() == null) {
                actualNote.getContentAsObject().setPins(new ArrayList<>());
            }
            actualNote.getContentAsObject().getPins().forEach(this::renderPin);
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
                measuredLength.setTitle(i18n.getString("MeasureLengthDialogTitle"));

                double distance = getDistance(rulerStartX, rulerStartY, rulerEndX, rulerEndY);

                measuredLength.setHeaderText(String.format(i18n.getString("MeasureLengthDialogHint"), distance));
                Optional<String> result = measuredLength.showAndWait();
                if (result.isPresent()) {

                    // Skalierungsfaktor = 1 zu X
                    double uniformedFactor = Double.valueOf(result.get());

                    actualNote.getContentAsObject().scale = uniformedFactor / distance;
                    root.getChildren().remove(rulerLine);
                    rulerLine = null;
                }

            }
        });

        root.setOnMouseDragged(e -> {
            switch (mode) {
                case FOG -> {
                    System.out.println(e.getButton());
                    if (actualNote.getContentAsObject().getFog() == null) {
                        actualNote.getContentAsObject().setFog(new ArrayList<>());
                    }
                    if (e.getButton() == MouseButton.PRIMARY) {
                        actualNote.getContentAsObject().getFog().add(new Point2D(e.getX(), e.getY()));
                    } else {
                        actualNote.getContentAsObject().getFog().stream().filter(p -> p.getX() == e.getX() && p.getY() == e.getY()).findFirst().ifPresent(point -> {
                            actualNote.getContentAsObject().getFog().remove(point);
                        });
                    }
                    e.consume();
                    updateView();
                }
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
                            if (actualNote.getContentAsObject().scale != 0) {
                                double pixelDistance = getDistance(rulerStartX, rulerStartY, rulerEndX, rulerEndY);
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
                        n.setX(e.getX());
                        n.setY(e.getY());
                        n.setLabel(i18n.getString("NewPin"));
                        n.setNoteReference(null);
                        actualNote.getContentAsObject().getPins().add(n);
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
        deletePinButton = new Button(i18n.getString("RemovePin"));


        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinLabel")));
        mapPropertiesPane.getChildren().add(labelProp);

        labelProp.onActionProperty().set(e -> {
            if (selectedPin != null) {
                selectedPin.setLabel(labelProp.getText());
                updateView();
            }

        });

        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinColor")));
        mapPropertiesPane.getChildren().add(colorProp);

        colorProp.onActionProperty().set(e -> {
            if (selectedPin != null) {
                selectedPin.setColor(colorProp.getValue());
                updateView();
            }
        });

        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinLink")));
        mapPropertiesPane.getChildren().add(noteReferenceProp);

        noteReferenceProp.onActionProperty().set(e -> {
            Note selected = noteReferenceProp.getValue();
            if (selectedPin != null && selected != null) {
                selectedPin.setNoteReference(selected.getReference());
                updateView();
            }
        });


        mapPropertiesPane.getChildren().add(new Separator());

        mapPropertiesPane.getChildren().add(deletePinButton);

        deletePinButton.onActionProperty().set(e -> {
            if (selectedPin != null) {
                actualNote.getContentAsObject().getPins().remove(selectedPin);
                selectedPin = null;
                updateView();
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

        noteReferenceProp.setCellFactory(noteListView -> new NotesRenderer());

    }

    void updateEditor() {
        populateNoteReferenceProp();

        labelProp.setText(selectedPin.getLabel());

        if (selectedPin.getNoteReference() != null) {
            noteReferenceProp.getSelectionModel().select(onNoteRequest.call(selectedPin.getNoteReference().toString()));
        }
        mapPropertiesPane.setVisible(true);
    }

    void renderPin(MapPin pin) {
        double pinSize = 32 * actualNote.getContentAsObject().getZoomFactor();
        if (pin.getColor() == null) {
            pin.setColor(Color.RED);
        }
        Button pinButton = new Button("", new FontIcon("icm-location:32:" + pin.getColor().toString()));
        pinButton.setBackground(Background.EMPTY);
        pinButton.setBorder(Border.EMPTY);
        pinButton.setLayoutX(pin.getX() - (pinSize/2));
        pinButton.setLayoutY(pin.getY() - (pinSize/2));
        pinButton.setPrefWidth(pinSize);
        pinButton.setPrefHeight(pinSize);
        pinButton.setCursor(Cursor.HAND);

        Tooltip p = new Tooltip();
        p.setText(pin.getLabel());


        pinButton.onMouseClickedProperty().set(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                selectedPin = pin;
                updateEditor();
            } else {
                if (pin.getNoteReference() != null) {
                    onNoteLoadRequest.call(pin.getNoteReference().toString());
                }
                mapPropertiesPane.setVisible(false);
            }
        });
        pinButton.setTooltip(p);
        root.getChildren().add(pinButton);
    }

    @Override
    public Callback<Boolean, MapNote> defineSaveCallback() {
        return p -> actualNote;
    }

    @Override
    public Callback<MapNote, Boolean> defineLoadCallback() {
        return param -> {
            actualNote = param;
            updateView();
            return true;
        };
    }

    @Override
    public Node getPreviewVersionOf(MapNote t) {

        VBox child = new VBox();

        ScrollPane p = new ScrollPane();
        p.setPannable(true);
        //p.setMaxWidth(width.get());

        if (actualNote != null) {

            FileAccessLayer.getInstance().getImageFromString(actualNote.getContentAsObject().backgroundPath).ifPresent(entry -> {

                actualNote.getContentAsObject().setBackgroundPath(entry.getKey());
                ImageView view = new ImageView(entry.getValue());

                view.setPreserveRatio(true);
                view.setFitHeight(view.getImage().getHeight() /2);

                view.onMouseClickedProperty().set(e -> {
                    if (e.getClickCount() == 2) {
                        e.consume();
                    }
                });

                p.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                p.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

                p.setContent(view);
                p.setPrefHeight(400);
            });

        }

        child.getChildren().add(p);

        return child;
    }

    @Override
    public Node getStandaloneVersion(MapNote t, Stage wnd) {
        return getPreviewVersionOf(t);
    }

    @Override
    public Note createNewNote() {
        return new MapNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("map", MapNote.class);
    }


    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}
