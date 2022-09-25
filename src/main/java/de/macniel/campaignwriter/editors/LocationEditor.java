package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
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

public class LocationEditor implements EditorPlugin {

    LocationNoteDefinition notesStructure;
    private TextField locationNameProp;
    private ComboBox<Note> pictureProp;
    private TextArea historyProp;
    private TextArea descriptionProp;
    private TextArea ambianceProp;
    private ComboBox<Note> parentLocationProp;
    private TextField locationCanonicalNameProp;
    private Note actualNote;

    private Callback onNoteLoadRequest;

    @Override
    public NoteType defineHandler() {
        return NoteType.LOCATION_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
        t.getItems().clear();
        t.setVisible(false);
    }

    @Override
    public Node defineEditor() {

        VBox box = new VBox();

        HBox locationNamePropLine = new HBox();
        Label locationNamePropLineLabel = new Label("Name");
        locationNamePropLineLabel.setPrefWidth(120);
        locationNameProp = new TextField();
        locationNamePropLine.getChildren().addAll(locationNamePropLineLabel, locationNameProp);
        HBox.setHgrow(locationNameProp, Priority.ALWAYS);
        HBox locationCanonicalNamePropLine = new HBox();
        Label locationCanonicalNamePropLineLabel = new Label("AKA");
        locationCanonicalNamePropLineLabel.setPrefWidth(120);
        locationCanonicalNameProp = new TextField();
        HBox.setHgrow(locationCanonicalNameProp, Priority.ALWAYS);
        locationCanonicalNamePropLine.getChildren().addAll(locationCanonicalNamePropLineLabel, locationCanonicalNameProp);

        HBox parentLocationPropLine = new HBox();
        Label parentLocationPropLineLabel = new Label("Region");
        parentLocationPropLineLabel.setPrefWidth(120);
        parentLocationPropLineLabel.setPrefWidth(120);
        parentLocationProp = new ComboBox<>();
        parentLocationPropLine.getChildren().addAll(parentLocationPropLineLabel, parentLocationProp);

        HBox ambiancePropLine = new HBox();
        Label ambiancePropLineLabel = new Label("Ambiente");
        ambiancePropLineLabel.setPrefWidth(120);
        ambianceProp = new TextArea();
        ambiancePropLine.getChildren().addAll(ambiancePropLineLabel, ambianceProp);
        HBox.setHgrow(ambianceProp, Priority.ALWAYS);
        VBox.setVgrow(ambianceProp, Priority.ALWAYS);
        HBox descriptionPropLine = new HBox();
        Label descriptionPropLineLabel = new Label("Beschreibung");
        descriptionPropLineLabel.setPrefWidth(120);
        descriptionProp = new TextArea();
        HBox.setHgrow(descriptionProp, Priority.ALWAYS);
        VBox.setVgrow(descriptionProp, Priority.ALWAYS);
        descriptionPropLine.getChildren().addAll(descriptionPropLineLabel, descriptionProp);

        HBox historyPropLine = new HBox();
        Label historyPropLineLabel = new Label("Hintergrund");
        historyPropLineLabel.setPrefWidth(120);
        historyProp = new TextArea();
        HBox.setHgrow(historyProp, Priority.ALWAYS);
        VBox.setVgrow(historyProp, Priority.ALWAYS);
        historyPropLine.getChildren().addAll(historyPropLineLabel, historyProp);

        HBox picturePropLine = new HBox();
        Label picturePropLineLabel = new Label("Aussehen");
        picturePropLineLabel.setPrefWidth(120);
        pictureProp = new ComboBox<>();
        picturePropLine.getChildren().addAll(picturePropLineLabel, pictureProp);


        parentLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, o, newValue) -> {
            if (newValue != null) {
                notesStructure.parentLocation = newValue.reference;
            } else {
                notesStructure.parentLocation = null;
            }
            saveTo();
        });

        ambianceProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.ambiance = newValue;
            saveTo();
        });

        historyProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.history = newValue;
            saveTo();
        });

        descriptionProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.description = newValue;
            saveTo();
        });

        locationNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.name = newValue;
            saveTo();
        });

        locationCanonicalNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            notesStructure.canonicalName = newValue;
            saveTo();
        });

        box.getChildren().addAll(locationNamePropLine, locationCanonicalNamePropLine, parentLocationPropLine, ambiancePropLine, descriptionPropLine, historyPropLine, picturePropLine);

        updateScroll();

        return box;
    }

    private void updateScroll() {

        if (notesStructure == null) {
            notesStructure = new LocationNoteDefinition();
        }

            List<Note> locationNotes = new ArrayList<>(FileAccessLayer
                    .getInstance()
                    .getAllNotes()
                    .stream()
                    .filter(n -> n.type == NoteType.LOCATION_NOTE)
                    .filter(n -> n != actualNote)
                    .toList());
        locationNotes.add(0, null);
            parentLocationProp.setItems(FXCollections.observableArrayList(locationNotes));
            FileAccessLayer.getInstance().findByReference(notesStructure.parentLocation).ifPresent(parentLocation -> {
                parentLocationProp.getSelectionModel().select(parentLocation);
            });

            locationNameProp.setText(notesStructure.name);
            locationCanonicalNameProp.setText(notesStructure.canonicalName);
            ambianceProp.setText(notesStructure.ambiance);
            descriptionProp.setText(notesStructure.description);
            historyProp.setText(notesStructure.history);


    }

    private void saveTo() {

        if (actualNote != null) {
            actualNote.content = FileAccessLayer.getInstance().getParser().toJson(notesStructure);
        }
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                actualNote = note;
                saveTo();
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                actualNote = note;
                notesStructure = FileAccessLayer.getInstance().getParser().fromJson(actualNote.content, LocationNoteDefinition.class);
                updateScroll();

                return true;
            }
        };
    }

    @Override
    public Node getPreviewVersionOf(Object t) {
        return null;
    }

    @Override
    public void setOnNoteLoadRequest(Callback stringBooleanCallback) {
        this.onNoteLoadRequest = stringBooleanCallback;
        
    }

    @Override
    public void setOnNoteRequest(Callback stringNoteCallback) {
        // TODO Auto-generated method stub
        
    }

}
