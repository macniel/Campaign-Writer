package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.SDK.types.MapNote;
import de.macniel.campaignwriter.SDK.types.MapPin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

public class MapNoteEditor extends EditorPlugin<MapNote> implements ViewerPlugin<MapNote> {
    private final ResourceBundle i18n;
    MapPin draggedElement;
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
    private Button selectedPinButton;
    private Button updatePinButton;
    private Callback<UUID, Boolean> requester;

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
        ToggleButton pointerModeButton = new ToggleButton("", new FontIcon("icm-location"));
        ToggleButton fogModeButton = new ToggleButton("", new FontIcon("icm-cloud"));

        Button loadButton = new Button("", new FontIcon("icm-image"));
        loadButton.onActionProperty().set(e -> {
            try {
                FileChooser dialog = new FileChooser();
                File file = dialog.showOpenDialog(w);
                new FileAccessLayerFactory().get().getImageFromString(file.getAbsolutePath()).ifPresent(entry -> {
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

        modeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
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
            new FileAccessLayerFactory().get().getImageFromString(actualNote.getContentAsObject().backgroundPath).ifPresent(entry -> {
                WritableImage image = new WritableImage(entry.getValue().getPixelReader(), (int) entry.getValue().getWidth(), (int) entry.getValue().getHeight());
                if (actualNote.getContentAsObject().getFog() == null) {
                    actualNote.getContentAsObject().setFog(new ArrayList<>());
                }
                actualNote.getContentAsObject().getFog().forEach(point -> {

                    int fogColor = 50 << 24;

                    image.getPixelWriter().setArgb((int) point.getX(), (int) point.getY(), fogColor);

                });

                if (backgroundLayer.getImage() == null || !backgroundLayer.getImage().equals(image)) {
                    backgroundLayer.imageProperty().set(image);
                    viewer.setHvalue(actualNote.getContentAsObject().getScrollPositionX());
                    viewer.setVvalue(actualNote.getContentAsObject().getScrollPositionY());
                }
            });


            refreshDragAndDropHandler();

            if (actualNote.getContentAsObject().getPins() == null) {
                actualNote.getContentAsObject().setPins(new ArrayList<>());
            }
            actualNote.getContentAsObject().getPins().forEach(this::renderPin);
        }
    }

    double getDistance(double sX, double sY, double eX, double eY) {
        return Math.sqrt(
                Math.pow((Math.max(sX, eX) - Math.min(eX, sX)), 2) +
                        Math.pow((Math.max(sY, eY) - Math.min(eY, sY)), 2));

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
        viewer.hvalueProperty().addListener(scrollEvent -> {
            actualNote.getContentAsObject().setScrollPositionX(viewer.getHvalue());
        });
        viewer.vvalueProperty().addListener(scrollEvent -> {
            actualNote.getContentAsObject().setScrollPositionY(viewer.getVvalue());
        });
        viewer.setPannable(true);

        backgroundLayer.setOnMouseClicked(e -> {
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
                case SCALE -> {
                }
            }
        });

        mapPropertiesPane = new VBox();

        labelProp = new TextField();
        colorProp = new ColorPicker();
        noteReferenceProp = new ComboBox<Note>();
        deletePinButton = new Button(i18n.getString("RemovePin"));

        updatePinButton = new Button("update Pin");


        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinLabel")));
        mapPropertiesPane.getChildren().add(labelProp);

        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinColor")));
        mapPropertiesPane.getChildren().add(colorProp);

        mapPropertiesPane.getChildren().add(new Label(i18n.getString("PinLink")));
        mapPropertiesPane.getChildren().add(noteReferenceProp);

        mapPropertiesPane.getChildren().add(new Separator());

        mapPropertiesPane.getChildren().add(updatePinButton);

        mapPropertiesPane.getChildren().add(deletePinButton);

        updatePinButton.onActionProperty().set(e -> {
            if (selectedPin != null) {
                System.out.println(actualNote.getContentAsObject().getPins().indexOf(selectedPin));
                if (noteReferenceProp.getValue() != null) {
                    selectedPin.setNoteReference(noteReferenceProp.getValue().getReference());
                } else {
                    selectedPin.setNoteReference(null);
                }
                selectedPin.setColor(colorProp.getValue());
                selectedPin.setLabel(labelProp.getText());
                updateView();
                mapPropertiesPane.setVisible(false);
            }
        });

        deletePinButton.onActionProperty().set(e -> {
            if (selectedPin != null) {
                actualNote.getContentAsObject().getPins().remove(selectedPin);
                root.getChildren().remove(selectedPinButton);
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
        List<Note> notes = new FileAccessLayerFactory().get().getAllNotes(); // TODO: this should not be possible, request maincontroller instead
        noteReferenceProp.setItems(FXCollections.observableArrayList(notes));
        noteReferenceProp.getItems().add(0, null);
        noteReferenceProp.setCellFactory(noteListView -> new NotesRenderer());
        noteReferenceProp.setButtonCell(new NotesRenderer());

    }

    void updateEditor() {
        populateNoteReferenceProp();

        labelProp.setText(selectedPin.getLabel());
        System.out.println("null?" + selectedPin.getNoteReference());

        if (selectedPin.getNoteReference() != null) {
            new FileAccessLayerFactory().get().findByReference(selectedPin.getNoteReference()).ifPresent(note -> {
                noteReferenceProp.getSelectionModel().select(note);
            });
        } else {
            System.out.println("null");
            noteReferenceProp.getSelectionModel().clearSelection();
            noteReferenceProp.setValue(null);
        }

        colorProp.setValue(selectedPin.getColor());

        mapPropertiesPane.setVisible(true);
    }

    void renderPin(MapPin pin) {
        System.out.println("render " + pin + " pointing " + pin.getNoteReference());
        double pinSize = 32 * actualNote.getContentAsObject().getZoomFactor();
        if (pin.getColor() == null) {
            pin.setColor(Color.RED);
        }
        Button pinButton = new Button("", new FontIcon("icm-location:32:" + pin.getColor().toString()));
        pinButton.setBackground(Background.EMPTY);
        pinButton.setBorder(Border.EMPTY);
        pinButton.setLayoutX(pin.getX() - (pinSize / 2));
        pinButton.setLayoutY(pin.getY() - (pinSize / 2));
        pinButton.setPrefWidth(pinSize);
        pinButton.setPrefHeight(pinSize);
        pinButton.setCursor(Cursor.HAND);

        Tooltip p = new Tooltip();
        p.setText(pin.getLabel());


        pinButton.onMouseClickedProperty().set(e -> {
            selectedPin = pin;
            selectedPinButton = pinButton;
            updateEditor();
            e.consume();
        });

        backgroundLayer.onDragEnteredProperty().set(e -> {

            System.out.println("dragging");
        });

        backgroundLayer.onDragDroppedProperty().set(e -> {
            e.acceptTransferModes(TransferMode.MOVE);
            System.out.println("Drag ended: " + e.getX() + " " + e.getY());
            if (draggedElement != null) {
                draggedElement.setX(e.getX());
                draggedElement.setY(e.getY());
                updateView();
            }

        });

        pinButton.onDragDetectedProperty().set(e -> {
            System.out.println("Drag detected");
            pinButton.startDragAndDrop(TransferMode.MOVE);

            draggedElement = pin;
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

        if (t != null) {

            new FileAccessLayerFactory().get().getImageFromString(t.getContentAsObject().backgroundPath).ifPresent(entry -> {

                t.getContentAsObject().setBackgroundPath(entry.getKey());
                ImageView view = new ImageView(entry.getValue());

                view.setPreserveRatio(true);
                view.setFitHeight(view.getImage().getHeight() / 2);

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

        BorderPane bp = new BorderPane();
        ImageView view = new ImageView();

        ScrollPane p = new ScrollPane();
        p.setPannable(true);

        ObservableList<MapPin> pins = FXCollections.observableArrayList(t.getContentAsObject().getPins());

        ListView<MapPin> pinListView = new ListView<>(pins);
        pinListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(MapPin item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    Label text = new Label(item.getLabel());
                    super.setGraphic(text);
                    text.onMouseClickedProperty().set(e -> {
                        if (e.getButton() == MouseButton.SECONDARY) {
                            // TODO: Open pin reference in another standalone window
                            if (requester != null) {
                                requester.call(item.getNoteReference());
                            }
                        }
                    });
                }
            }
        });

        pinListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                System.out.println(view.getImage().getWidth() / newValue.getX() + " x " + view.getImage().getHeight() / newValue.getY());
                p.setHvalue(newValue.getX() / view.getImage().getWidth());
                p.setVvalue(newValue.getY() / view.getImage().getHeight());
            }
        });

        //p.setMaxWidth(width.get());

        new FileAccessLayerFactory().get().getImageFromString(t.getContentAsObject().backgroundPath).ifPresent(entry -> {

            t.getContentAsObject().setBackgroundPath(entry.getKey());
            view.setImage(entry.getValue());

            view.setPreserveRatio(true);
            view.setFitHeight(view.getImage().getHeight() / 2);

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

        bp.setCenter(p);
        bp.setRight(pinListView);

        return bp;
    }

    @Override
    public Note createNewNote() {
        return new MapNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerViewer(this);
        registry.registerType("map", MapNote.class);
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;

    }


    private enum Mode {
        POINTER,
        MEASURE,
        FOG, SCALE
    }
}
