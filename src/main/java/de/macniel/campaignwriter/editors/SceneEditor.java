package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.ActorNoteRenderer;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.LocationNoteRenderer;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.NotesRenderer;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.Objects;
import java.util.ResourceBundle;
import java.util.UUID;

public class SceneEditor extends EditorPlugin<SceneNote> implements ViewerPlugin<SceneNote> {

    private final ResourceBundle i18n;

    SceneNote actualNote;

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
        VBox box = new VBox();

        HBox shortDescriptionPropLine = new HBox();
        Label shortDescriptionPropLineLabel = new Label(i18n.getString("Introduction"));
        shortDescriptionPropLineLabel.setPrefWidth(120);
        shortDescriptionPropLine.getChildren().add(shortDescriptionPropLineLabel);
        shortDescriptionProp = new TextField();

        shortDescriptionProp.textProperty().addListener( (observable, oldText, newText) -> {
            actualNote.getContentAsObject().setShortDescription(newText);
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

        locationProp.setCellFactory(noteListView -> new LocationNoteRenderer());
        locationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newLocation) -> {
            actualNote.getContentAsObject().setLocation(newLocation.getReference());
        });
        HBox.setHgrow(locationProp, Priority.ALWAYS);


        locationPropLine.getChildren().add(locationProp);

        HBox actorListPropLine = new HBox();
        Label actorListPropLineLabel = new Label(i18n.getString("ParticipatingActors"));
        actorListPropLineLabel.setPrefWidth(120);
        actorListPropLine.getChildren().add(actorListPropLineLabel);

        ObservableList<ActorNote> actors = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().equals("actor")).map(note -> (ActorNote) note).toList());

        actorListProp = new ListView<>(actors);
        actorListProp.setCellFactory(noteListView -> new ActorNoteRenderer());
        actorListProp.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        actorListProp.getSelectionModel().selectedItemProperty().addListener( (observable, oldValue, newValue) -> {
            actualNote.getContentAsObject().setActors(actorListProp.getSelectionModel().getSelectedItems().stream().map(actor -> actor.getReference()).toList());
        });

        actorListProp.setOrientation(Orientation.HORIZONTAL);
        actorListProp.setMinHeight(32);
        actorListProp.setMaxHeight(32);
        HBox.setHgrow(actorListProp, Priority.ALWAYS);



        actorListPropLine.getChildren().add(actorListProp);

        Label longDescription = new Label(i18n.getString("SceneDescription"));
        longDescriptionProp = new TextArea();
        longDescriptionProp.textProperty().addListener((observableValue, oldText, newText) -> {
            actualNote.getContentAsObject().setLongDescription(newText);
        });
        longDescriptionProp.setWrapText(true);
        HBox.setHgrow(longDescriptionProp, Priority.ALWAYS);
        VBox.setVgrow(longDescriptionProp, Priority.ALWAYS);

        box.getChildren().addAll(shortDescriptionPropLine, locationPropLine, actorListPropLine, longDescription, longDescriptionProp);


        return box;
    }

    @Override
    public Callback<Boolean, SceneNote> defineSaveCallback() {
        return note -> actualNote;
    }

    @Override
    public Callback<SceneNote, Boolean> defineLoadCallback() {
        return note -> {
            actualNote = note;
            updateView();
            return true;
        };
    }

    void updateView() {
        if (actualNote != null) {
            shortDescriptionProp.setText(actualNote.getContentAsObject().getShortDescription());

            if (actualNote.getContentAsObject().getLocation() != null) {
                FileAccessLayer.getInstance().findByReference(actualNote.getContentAsObject().getLocation()).ifPresent(location -> {
                        locationProp.getSelectionModel().clearSelection();
                        locationProp.getSelectionModel().select((LocationNote) location);
                });
            }
            MultipleSelectionModel<ActorNote> selectionModel = actorListProp.getSelectionModel();
            actualNote.getContentAsObject().getActors().forEach(actorReference -> {
                FileAccessLayer.getInstance().findByReference(actorReference).ifPresent(actor -> {
                    selectionModel.select(actorListProp.getItems().indexOf(actor));
                });
            });

            longDescriptionProp.setText(actualNote.getContentAsObject().getLongDescription());
        }
    }

    @Override
    public Node getPreviewVersionOf(SceneNote t) {
        return null;
    }

    @Override
    public Node getStandaloneVersion(SceneNote t, Stage wnd) {
        return null;
    }

    @Override
    public Note createNewNote() {
        return new SceneNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("scene", SceneNote.class);
    }


    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}
