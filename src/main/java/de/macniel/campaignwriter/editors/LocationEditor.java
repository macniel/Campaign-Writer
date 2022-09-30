package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.types.Location;
import de.macniel.campaignwriter.types.LocationNote;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class LocationEditor extends EditorPlugin<LocationNote> {

    Location notesStructure;
    private TextField locationNameProp;
    private ComboBox<Note> pictureProp;
    private TextArea historyProp;
    private TextArea descriptionProp;
    private TextArea ambianceProp;
    private ComboBox<Note> parentLocationProp;
    private TextField locationCanonicalNameProp;

    private Callback onNoteLoadRequest;
    private ResourceBundle i18n;

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
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

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
        picturePropLine.getChildren().addAll(picturePropLineLabel, pictureProp);


        parentLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, o, newValue) -> {
            if (newValue != null) {
                notesStructure.setParentLocation(newValue.reference);
            } else {
                notesStructure.setParentLocation(null);
            }
        });

        ambianceProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.setAmbiance(newValue);
        });

        historyProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.setHistory(newValue);
        });

        descriptionProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.setDescription(newValue);
        });

        locationNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.setName(newValue);
        });

        locationCanonicalNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.setCanonicalName(newValue);
        });

        box.getChildren().addAll(locationNamePropLine, locationCanonicalNamePropLine, parentLocationPropLine, ambiancePropLine, descriptionPropLine, historyPropLine, picturePropLine);

        updateView();

        return box;
    }

    private void updateView() {

        if (notesStructure == null) {
            notesStructure = new Location();
        }

            List<Note> locationNotes = new ArrayList<>(FileAccessLayer
                    .getInstance()
                    .getAllNotes()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(n -> {
                        return n.getType().equals("location");
                    })
                    .filter(n -> n != notesStructure)
                    .toList());
        locationNotes.add(0, null);
            parentLocationProp.setItems(FXCollections.observableArrayList(locationNotes));
            FileAccessLayer.getInstance().findByReference(notesStructure.getParentLocation()).ifPresent(parentLocation -> {
                parentLocationProp.getSelectionModel().select(parentLocation);
            });

            locationNameProp.setText(notesStructure.getName());
            locationCanonicalNameProp.setText(notesStructure.getCanonicalName());
            ambianceProp.setText(notesStructure.getAmbiance());
            descriptionProp.setText(notesStructure.getDescription());
            historyProp.setText(notesStructure.getHistory());


    }

    @Override
    public Callback<Boolean, Note> defineSaveCallback() {
        return note -> notesStructure;
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return note -> {
            notesStructure = (Location) note;
            updateView();
            return true;
        };
    }

    @Override
    public Node getPreviewVersionOf(LocationNote t) {
        System.out.println("Rendering Location " + t.content.getCanonicalName() + " as standalone");
        return new HBox(new Label("Willkommen in " + t.content.getCanonicalName()));
    }

    @Override
    public Node getStandaloneVersion(LocationNote t) {
        return getPreviewVersionOf(t);
    }

    @Override
    public Note createNewNote() {
        return new Location();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
    }
}
