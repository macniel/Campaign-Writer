package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.adapters.ColorAdapter;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.ColorInput;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class MapNoteEditor implements EditorPlugin {
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
        BorderPane bp = new BorderPane();
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
                selectedPin = n;
                renderPin(n);
                updateEditor();
            } else {
                System.out.println("Root clicked");
                selectedPin = null;
                mapPropertiesPane.setVisible(false);
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

        return bp;
    }

    void populateNoteReferenceProp() {
        ArrayList<Note> notes = Note.getAll(); // TODO: this should not be possible, request maincontroller instead
        noteReferenceProp.setItems(FXCollections.observableArrayList(notes));

        Callback<ListView<Note>, ListCell<Note>> itemCellFactory = new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(ListView<Note> param) {
                return new ListCell<Note>() {

                    @Override
                    protected void updateItem(Note item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            setText(item.getLabel());
                        }
                    }
                } ;
            }
        };

        noteReferenceProp.setButtonCell(itemCellFactory.call(null));
        noteReferenceProp.setCellFactory(itemCellFactory);

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
}
