package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.LocationNoteRenderer;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.LocationNote;
import de.macniel.campaignwriter.SDK.types.PictureNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class LocationEditor extends EditorPlugin<LocationNote> implements ViewerPlugin<LocationNote> {

    private final ResourceBundle i18n;
    LocationNote actualNote;
    private TextField locationNameProp;
    private ComboBox<Note> pictureProp;
    private TextArea historyProp;
    private TextArea descriptionProp;
    private TextArea ambianceProp;
    private ComboBox<LocationNote> parentLocationProp;
    private TextField locationCanonicalNameProp;
    private Callback<UUID, Boolean> requester;

    public LocationEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

    @Override
    public String defineHandler() {
        return "building/location";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();
        t.setVisible(false);
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;
    }

    @Override
    public Node defineEditor() {

        VBox box = new VBox();

        HBox locationNamePropLine = new HBox();
        Label locationNamePropLineLabel = new Label(i18n.getString("LocationName"));
        locationNamePropLineLabel.setPrefWidth(120);
        locationNameProp = new TextField();
        locationNamePropLine.getChildren().addAll(locationNamePropLineLabel, locationNameProp);
        HBox.setHgrow(locationNameProp, Priority.ALWAYS);
        HBox locationCanonicalNamePropLine = new HBox();
        Label locationCanonicalNamePropLineLabel = new Label(i18n.getString("LocationCanonicalName"));
        locationCanonicalNamePropLineLabel.setPrefWidth(120);
        locationCanonicalNameProp = new TextField();
        HBox.setHgrow(locationCanonicalNameProp, Priority.ALWAYS);
        locationCanonicalNamePropLine.getChildren().addAll(locationCanonicalNamePropLineLabel, locationCanonicalNameProp);

        HBox parentLocationPropLine = new HBox();
        Label parentLocationPropLineLabel = new Label(i18n.getString("LocationRegionName"));
        parentLocationPropLineLabel.setPrefWidth(120);
        parentLocationPropLineLabel.setPrefWidth(120);
        parentLocationProp = new ComboBox<>();
        parentLocationProp.setButtonCell(new LocationNoteRenderer());
        parentLocationProp.setCellFactory(listView -> new LocationNoteRenderer());
        parentLocationPropLine.getChildren().addAll(parentLocationPropLineLabel, parentLocationProp);

        HBox ambiancePropLine = new HBox();
        Label ambiancePropLineLabel = new Label(i18n.getString("Ambiance"));
        ambiancePropLineLabel.setPrefWidth(120);
        ambianceProp = new TextArea();
        ambiancePropLine.getChildren().addAll(ambiancePropLineLabel, ambianceProp);
        HBox.setHgrow(ambianceProp, Priority.ALWAYS);
        VBox.setVgrow(ambianceProp, Priority.ALWAYS);
        HBox descriptionPropLine = new HBox();
        Label descriptionPropLineLabel = new Label(i18n.getString("Description"));
        descriptionPropLineLabel.setPrefWidth(120);
        descriptionProp = new TextArea();
        HBox.setHgrow(descriptionProp, Priority.ALWAYS);
        VBox.setVgrow(descriptionProp, Priority.ALWAYS);
        descriptionPropLine.getChildren().addAll(descriptionPropLineLabel, descriptionProp);

        HBox historyPropLine = new HBox();
        Label historyPropLineLabel = new Label(i18n.getString("Background"));
        historyPropLineLabel.setPrefWidth(120);
        historyProp = new TextArea();
        HBox.setHgrow(historyProp, Priority.ALWAYS);
        VBox.setVgrow(historyProp, Priority.ALWAYS);
        historyPropLine.getChildren().addAll(historyPropLineLabel, historyProp);

        HBox picturePropLine = new HBox();
        Label picturePropLineLabel = new Label(i18n.getString("LookAndFeel"));
        picturePropLineLabel.setPrefWidth(120);
        pictureProp = new ComboBox<>();
        pictureProp.setButtonCell(new NotesRenderer(true));
        pictureProp.setCellFactory(listView -> new NotesRenderer(true));


        ObservableList<Note> pictures = FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note instanceof PictureNote).toList());

        pictureProp.setItems(pictures);
        picturePropLine.getChildren().addAll(picturePropLineLabel, pictureProp);


        parentLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, o, newValue) -> {
            if (newValue != null) {
                actualNote.getContentAsObject().setParentLocation(newValue.getReference());
            } else {
                actualNote.getContentAsObject().setParentLocation(null);
            }
        });

        ambianceProp.textProperty().addListener((observableValue, s, newValue) -> {
            actualNote.getContentAsObject().setAmbiance(newValue);
        });

        historyProp.textProperty().addListener((observableValue, s, newValue) -> {
            actualNote.getContentAsObject().setHistory(newValue);
        });

        descriptionProp.textProperty().addListener((observableValue, s, newValue) -> {
            actualNote.getContentAsObject().setDescription(newValue);
        });

        locationNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            actualNote.getContentAsObject().setName(newValue);
        });

        locationCanonicalNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            actualNote.getContentAsObject().setCanonicalName(newValue);
        });

        pictureProp.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                actualNote.getContentAsObject().setPicture(newValue.getReference());
            } else {
                actualNote.getContentAsObject().setPicture(null);
            }
        });

        box.getChildren().addAll(locationNamePropLine, locationCanonicalNamePropLine, parentLocationPropLine, ambiancePropLine, descriptionPropLine, historyPropLine, picturePropLine);

        updateView();

        return box;
    }

    private void updateView() {
        if (actualNote != null) {
            List<LocationNote> locationNotes = new ArrayList<>(FileAccessLayer
                    .getInstance()
                    .getAllNotes()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(n -> {
                        return n.getType().equals("location");
                    })
                    .map(n -> (LocationNote) n)
                    .filter(n -> n != actualNote)
                    .toList());
            locationNotes.add(0, null);
            parentLocationProp.setItems(FXCollections.observableArrayList(locationNotes));
            new FileAccessLayerFactory().get().findByReference(actualNote.getContentAsObject().getParentLocation()).ifPresent(parentLocation -> {
                parentLocationProp.getSelectionModel().select((LocationNote) parentLocation);
            });

            locationNameProp.setText(actualNote.getContentAsObject().getName());
            locationCanonicalNameProp.setText(actualNote.getContentAsObject().getCanonicalName());
            ambianceProp.setText(actualNote.getContentAsObject().getAmbiance());
            descriptionProp.setText(actualNote.getContentAsObject().getDescription());
            historyProp.setText(actualNote.getContentAsObject().getHistory());
            new FileAccessLayerFactory().get().findByReference(actualNote.getContentAsObject().getPicture()).ifPresent(pictureNote -> {
                pictureProp.setValue(pictureNote);
            });
        }
    }

    @Override
    public Callback<Boolean, LocationNote> defineSaveCallback() {
        return note -> actualNote;
    }

    @Override
    public Callback<LocationNote, Boolean> defineLoadCallback() {
        return note -> {
            actualNote = note;
            updateView();
            return true;
        };
    }

    @Override
    public Node getPreviewVersionOf(LocationNote t) {

        VBox box = new VBox();

        FileAccessLayerInterface fal = new FileAccessLayerFactory().get();

        fal.findByReference(t.getContentAsObject().getPicture())
                .flatMap(pictureNote -> fal.getImageFromString(((PictureNote) pictureNote).getContentAsObject().getFileName()))
                .ifPresent(entry -> {

                    ScrollPane header = new ScrollPane();
                    header.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    header.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    header.setFocusTraversable(false);

                    header.setMaxHeight(200);
                    header.setPrefHeight(200);

                    ImageView v = new ImageView();
                    v.setImage(entry.getValue());
                    header.setContent(v);
                    box.getChildren().add(header);
                });


        String name = t.getContentAsObject().getName();
        String akaName = t.getContentAsObject().getCanonicalName();

        Label locationName = new Label(name);
        if (akaName != null && !akaName.isEmpty()) {
            locationName.setText(name + " (AKA " + akaName + ")");
        }

        TextFlow description = new TextFlow(new Text(
                t.getContentAsObject().getDescription()
        ));


        box.getChildren().addAll(locationName, description);

        return box;
    }

    @Override
    public Node getStandaloneVersion(LocationNote t, Stage wnd) {

        VBox box = new VBox();

        FileAccessLayerInterface fal = new FileAccessLayerFactory().get();

        fal.findByReference(t.getContentAsObject().getPicture())
                .flatMap(pictureNote -> fal.getImageFromString(((PictureNote) pictureNote).getContentAsObject().getFileName()))
                .ifPresent(entry -> {

                    ScrollPane header = new ScrollPane();
                    header.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    header.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                    header.setFocusTraversable(false);

                    header.setMaxHeight(200);
                    header.setPrefHeight(200);

                    ImageView v = new ImageView();
                    v.setImage(entry.getValue());
                    header.setContent(v);
                    box.getChildren().add(header);
                });


        String name = t.getContentAsObject().getName();
        String akaName = t.getContentAsObject().getCanonicalName();

        Label locationName = new Label(name);
        if (akaName != null && !akaName.isEmpty()) {
            locationName.setText(name + " (AKA " + akaName + ")");
        }
        locationName.setPadding(new Insets(0, 0, 0, 10));

        TextFlow description = new TextFlow(new Text(
                t.getContentAsObject().getDescription()
        ));
        description.setPadding(new Insets(0, 0, 0, 20));


        Label ambianceLabel = new Label(i18n.getString("Ambiance"));
        ambianceLabel.setPadding(new Insets(0, 0, 0, 10));

        TextFlow ambiance = new TextFlow(new Text(
                t.getContentAsObject().getAmbiance()
        ));
        ambiance.setPadding(new Insets(0, 0, 0, 20));

        Label historyLabel = new Label(i18n.getString("Background"));
        historyLabel.setPadding(new Insets(0, 0, 0, 10));

        TextFlow history = new TextFlow(new Text(
                t.getContentAsObject().getHistory()
        ));
        history.setPadding(new Insets(0, 0, 0, 20));


        box.getChildren().addAll(locationName, description, ambianceLabel, ambiance, historyLabel, history);

        return box;
    }

    @Override
    public Note createNewNote() {
        return new LocationNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerViewer(this);
        registry.registerType("location", LocationNote.class);
    }
}
