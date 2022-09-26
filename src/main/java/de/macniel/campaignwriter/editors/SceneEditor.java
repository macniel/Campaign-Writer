package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.NotesRenderer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.ResourceBundle;
import java.util.UUID;

public class SceneEditor implements EditorPlugin<SceneNoteDefinition> {

    private final ResourceBundle i18n;
    SceneNoteDefinition notesStructure;

    Note actualNote;

    private ComboBox<Note> locationProp;
    private TextField shortDescriptionProp;
    private ListView<Note> actorListProp;
    private TextArea longDescriptionProp;

    @Override
    public NoteType defineHandler() {
        return NoteType.SCENE_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
        t.getItems().clear();
        t.setVisible(false);
    }

    public SceneEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

    @Override
    public Node defineEditor() {
        if (notesStructure == null) {
            notesStructure = new SceneNoteDefinition();
        }

        VBox box = new VBox();

        HBox shortDescriptionPropLine = new HBox();
        Label shortDescriptionPropLineLabel = new Label(i18n.getString("Introduction"));
        shortDescriptionPropLineLabel.setPrefWidth(120);
        shortDescriptionPropLine.getChildren().add(shortDescriptionPropLineLabel);
        shortDescriptionProp = new TextField();

        shortDescriptionProp.textProperty().addListener( (observable, oldText, newText) -> {
            if (notesStructure == null) {
                notesStructure = new SceneNoteDefinition();
            }
            notesStructure.shortDescription = newText;
        });
        shortDescriptionPropLine.getChildren().add(shortDescriptionProp);
        HBox.setHgrow(shortDescriptionProp, Priority.ALWAYS);

        HBox locationPropLine = new HBox();
        Label locationPropLineLabel = new Label(i18n.getString("Location"));
        locationPropLineLabel.setPrefWidth(120);
        locationPropLine.getChildren().add(locationPropLineLabel);


        ObservableList<Note> locations = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.type == NoteType.LOCATION_NOTE).toList());

        locationProp = new ComboBox<>(locations);
        locationProp.getSelectionModel().clearSelection();

        //locationProp.setCellFactory(noteListView -> new NotesRenderer());
        locationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newLocation) -> {
            if (notesStructure == null) {
                notesStructure = new SceneNoteDefinition();
            }
            notesStructure.location = newLocation;
        });
        HBox.setHgrow(locationProp, Priority.ALWAYS);


        locationPropLine.getChildren().add(locationProp);

        HBox actorListPropLine = new HBox();
        Label actorListPropLineLabel = new Label(i18n.getString("ParticipatingActors"));
        actorListPropLineLabel.setPrefWidth(120);
        actorListPropLine.getChildren().add(actorListPropLineLabel);

        ObservableList<Note> actors = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.type == NoteType.ACTOR_NOTE).toList());

        actorListProp = new ListView<>(actors);
        actorListProp.setCellFactory(noteListView -> new NotesRenderer());
        actorListProp.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        actorListProp.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            notesStructure.actors = actorListProp.getSelectionModel().getSelectedItems().stream().map(actor -> actor.reference).toList();
        });

        actorListProp.setOrientation(Orientation.HORIZONTAL);
        actorListProp.setMinHeight(32);
        actorListProp.setMaxHeight(32);
        HBox.setHgrow(actorListProp, Priority.ALWAYS);



        actorListPropLine.getChildren().add(actorListProp);

        Label longDescription = new Label(i18n.getString("SceneDescription"));
        longDescriptionProp = new TextArea();
        longDescriptionProp.textProperty().addListener((observableValue, oldText, newText) -> {
            if (notesStructure == null) {
                notesStructure = new SceneNoteDefinition();
            }
            notesStructure.longDescription = newText;
        });
        longDescriptionProp.setWrapText(true);
        HBox.setHgrow(longDescriptionProp, Priority.ALWAYS);
        VBox.setVgrow(longDescriptionProp, Priority.ALWAYS);

        box.getChildren().addAll(shortDescriptionPropLine, locationPropLine, actorListPropLine, longDescription, longDescriptionProp);


        return box;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                note.content = FileAccessLayer.getInstance().getParser().toJson(notesStructure);
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
                notesStructure = FileAccessLayer.getInstance().getParser().fromJson(actualNote.content, SceneNoteDefinition.class);
                updateFields();
                return true;
            }
        };
    }

    void updateFields() {
        if (notesStructure != null) {
            shortDescriptionProp.setText(notesStructure.shortDescription);
            if (notesStructure.location.type == NoteType.LOCATION_NOTE) {
                locationProp.getSelectionModel().clearSelection();
                locationProp.getSelectionModel().select(notesStructure.location);
            }
            MultipleSelectionModel<Note> selectionModel = actorListProp.getSelectionModel();
            for (UUID actorReference : notesStructure.actors) {
                FileAccessLayer.getInstance().findByReference(actorReference).ifPresent(actor -> {
                    selectionModel.select(actorListProp.getItems().indexOf(actor));
                });
            };

            longDescriptionProp.setText(notesStructure.longDescription);
        }
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }

    @Override
    public Node getPreviewVersionOf(SceneNoteDefinition t) {
        return null;
    }
}
