package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.types.ActorNote;
import de.macniel.campaignwriter.types.LocationNote;
import de.macniel.campaignwriter.types.Scene;
import de.macniel.campaignwriter.types.SceneNote;
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

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class SceneEditor extends EditorPlugin<SceneNote> {

    private final ResourceBundle i18n;
    SceneNote notesStructure;

    private ComboBox<LocationNote> locationProp;
    private TextField shortDescriptionProp;
    private ListView<ActorNote> actorListProp;
    private TextArea longDescriptionProp;

    @Override
    public String defineHandler() {
        return "building/scene";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();
        t.setVisible(false);
    }

    public SceneEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

    @Override
    public Node defineEditor() {
        if (notesStructure == null) {
            notesStructure = new SceneNote();
        }

        VBox box = new VBox();

        HBox shortDescriptionPropLine = new HBox();
        Label shortDescriptionPropLineLabel = new Label(i18n.getString("Introduction"));
        shortDescriptionPropLineLabel.setPrefWidth(120);
        shortDescriptionPropLine.getChildren().add(shortDescriptionPropLineLabel);
        shortDescriptionProp = new TextField();

        shortDescriptionProp.textProperty().addListener( (observable, oldText, newText) -> {
            if (notesStructure == null) {
                notesStructure = new SceneNote();
            }
            notesStructure.content.setShortDescription(newText);
        });
        shortDescriptionPropLine.getChildren().add(shortDescriptionProp);
        HBox.setHgrow(shortDescriptionProp, Priority.ALWAYS);

        HBox locationPropLine = new HBox();
        Label locationPropLineLabel = new Label(i18n.getString("Location"));
        locationPropLineLabel.setPrefWidth(120);
        locationPropLine.getChildren().add(locationPropLineLabel);


        ObservableList<LocationNote> locations = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(Objects::nonNull).filter(note -> note.getType().equals("location")).map(note -> (LocationNote) note).toList());

        locationProp = new ComboBox<>(locations);
        locationProp.getSelectionModel().clearSelection();

        //locationProp.setCellFactory(noteListView -> new NotesRenderer());
        locationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newLocation) -> {
            if (notesStructure == null) {
                notesStructure = new SceneNote();
            }
            notesStructure.content.setLocation(newLocation.reference);
        });
        HBox.setHgrow(locationProp, Priority.ALWAYS);


        locationPropLine.getChildren().add(locationProp);

        HBox actorListPropLine = new HBox();
        Label actorListPropLineLabel = new Label(i18n.getString("ParticipatingActors"));
        actorListPropLineLabel.setPrefWidth(120);
        actorListPropLine.getChildren().add(actorListPropLineLabel);

        ObservableList<ActorNote> actors = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().equals("actor")).map(note -> (ActorNote) note).toList());

        actorListProp = new ListView<>(actors);
        //actorListProp.setCellFactory(noteListView -> new NotesRenderer());
        actorListProp.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        actorListProp.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            notesStructure.content.setActors(actorListProp.getSelectionModel().getSelectedItems().stream().map(actor -> actor.reference).toList());
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
                notesStructure = new SceneNote();
            }
            notesStructure.content.setLongDescription(newText);
        });
        longDescriptionProp.setWrapText(true);
        HBox.setHgrow(longDescriptionProp, Priority.ALWAYS);
        VBox.setVgrow(longDescriptionProp, Priority.ALWAYS);

        box.getChildren().addAll(shortDescriptionPropLine, locationPropLine, actorListPropLine, longDescription, longDescriptionProp);


        return box;
    }

    @Override
    public Callback<Boolean, SceneNote> defineSaveCallback() {
        return note -> notesStructure;
    }

    @Override
    public Callback<SceneNote, Boolean> defineLoadCallback() {
        return note -> {
            notesStructure = note;
            updateView();
            return true;
        };
    }

    void updateView() {
        if (notesStructure != null) {
            shortDescriptionProp.setText(notesStructure.content.getShortDescription());

            if (notesStructure.content.getLocation() != null) {
                FileAccessLayer.getInstance().findByReference(notesStructure.content.getLocation()).ifPresent(location -> {
                        locationProp.getSelectionModel().clearSelection();
                        locationProp.getSelectionModel().select((LocationNote) location);
                });
            }
            MultipleSelectionModel<ActorNote> selectionModel = actorListProp.getSelectionModel();
            for (UUID actorReference : notesStructure.content.getActors()) {
                FileAccessLayer.getInstance().findByReference(actorReference).ifPresent(actor -> {
                    selectionModel.select(actorListProp.getItems().indexOf(actor));
                });
            };

            longDescriptionProp.setText(notesStructure.content.getLongDescription());
        }
    }

    @Override
    public Node getPreviewVersionOf(SceneNote t) {
        return null;
    }

    @Override
    public Node getStandaloneVersion(SceneNote t) {
        return null;
    }

    @Override
    public Note createNewNote() {
        return new SceneNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
    }


    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}
