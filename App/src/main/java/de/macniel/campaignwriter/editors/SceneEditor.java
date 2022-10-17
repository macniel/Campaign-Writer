package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.ActorNoteRenderer;
import de.macniel.campaignwriter.LocationNoteRenderer;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.Actor;
import de.macniel.campaignwriter.SDK.types.ActorNote;
import de.macniel.campaignwriter.SDK.types.LocationNote;
import de.macniel.campaignwriter.SDK.types.SceneNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.*;

import static de.macniel.campaignwriter.FileAccessLayer.NAME_FIELD_NAME;
import static de.macniel.campaignwriter.FileAccessLayer.PORTRAIT_FIELD_NAME;

public class SceneEditor extends EditorPlugin<SceneNote> implements ViewerPlugin<SceneNote> {

    private final ResourceBundle i18n;
    SceneNote actualNote;

    private ComboBox<LocationNote> locationProp;
    private TextField shortDescriptionProp;
    private ListView<ActorNote> actorListProp;
    private TextArea longDescriptionProp;
    private Callback<UUID, Boolean> requester;

    public SceneEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

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

    @Override
    public Node defineEditor() {
        VBox box = new VBox();

        HBox shortDescriptionPropLine = new HBox();
        Label shortDescriptionPropLineLabel = new Label(i18n.getString("Introduction"));
        shortDescriptionPropLineLabel.setPrefWidth(120);
        shortDescriptionPropLine.getChildren().add(shortDescriptionPropLineLabel);
        shortDescriptionProp = new TextField();

        shortDescriptionProp.textProperty().addListener((observable, oldText, newText) -> {
            actualNote.getContentAsObject().setShortDescription(newText);
        });
        shortDescriptionPropLine.getChildren().add(shortDescriptionProp);
        HBox.setHgrow(shortDescriptionProp, Priority.ALWAYS);

        HBox locationPropLine = new HBox();
        Label locationPropLineLabel = new Label(i18n.getString("Location"));
        locationPropLineLabel.setPrefWidth(120);
        locationPropLine.getChildren().add(locationPropLineLabel);


        ObservableList<LocationNote> locations = FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(Objects::nonNull).filter(note -> note.getType().equals("location")).map(note -> (LocationNote) note).toList());

        locationProp = new ComboBox<>(locations);

        locationProp.getSelectionModel().clearSelection();

        locationProp.setCellFactory(noteListView -> new LocationNoteRenderer());
        locationProp.setButtonCell(new LocationNoteRenderer());
        locationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newLocation) -> {
            actualNote.getContentAsObject().setLocation(newLocation.getReference());
        });
        HBox.setHgrow(locationProp, Priority.ALWAYS);


        locationPropLine.getChildren().add(locationProp);

        HBox actorListPropLine = new HBox();
        Label actorListPropLineLabel = new Label(i18n.getString("ParticipatingActors"));
        actorListPropLineLabel.setPrefWidth(120);
        actorListPropLine.getChildren().add(actorListPropLineLabel);

        ObservableList<ActorNote> actors = FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().equals("actor")).map(note -> (ActorNote) note).toList());

        actorListProp = new ListView<>(actors);
        actorListProp.setCellFactory(noteListView -> new ActorNoteRenderer());
        actorListProp.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        actorListProp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
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
                new FileAccessLayerFactory().get().findByReference(actualNote.getContentAsObject().getLocation()).ifPresent(location -> {
                    locationProp.getSelectionModel().clearSelection();
                    locationProp.getSelectionModel().select((LocationNote) location);
                });
            }
            MultipleSelectionModel<ActorNote> selectionModel = actorListProp.getSelectionModel();
            actualNote.getContentAsObject().getActors().forEach(actorReference -> {
                new FileAccessLayerFactory().get().findByReference(actorReference).ifPresent(actor -> {
                    selectionModel.select(actorListProp.getItems().indexOf(actor));
                });
            });

            longDescriptionProp.setText(actualNote.getContentAsObject().getLongDescription());
        }
    }

    @Override
    public Node getPreviewVersionOf(SceneNote t) {
        VBox box = new VBox();
        box.setFillWidth(true);

        Label locationLabel = new Label();
        Label introductionLabel = new Label();
        HBox actors = new HBox();
        actors.setSpacing(20);

        new FileAccessLayerFactory().get().findByReference(t.getContentAsObject().getLocation()).ifPresent(locationNote -> {
            locationLabel.setText(((LocationNote) locationNote).getContentAsObject().getName());
        });

        actors.getChildren().add(new Label("Actors: "));

        introductionLabel.setText(t.getContentAsObject().getShortDescription());

        t.getContentAsObject().getActors().forEach(actorRef -> {
            FileAccessLayerInterface fal = new FileAccessLayerFactory().get();
            fal.findByReference(actorRef).ifPresent(actor -> {
                Actor a = (Actor) actor.getContentAsObject();
                a.getItems().stream().filter(e -> e.getLabel() != null && e.getLabel().equals(fal.getSetting(NAME_FIELD_NAME).get())).findFirst().ifPresent(name -> {
                    Label actorLink = new Label(name.getContent());
                    actorLink.getStyleClass().add("link");
                    actorLink.onMouseClickedProperty().set(ev -> {
                        if (requester != null) {
                            requester.call(actorRef);
                        }
                    });
                    actors.getChildren().add(actorLink);
                });
            });
        });

        box.getChildren().addAll(locationLabel, introductionLabel, actors);
        return box;

    }

    @Override
    public Node getStandaloneVersion(SceneNote t, Stage wnd) {
        FileAccessLayerInterface fal = new FileAccessLayerFactory().get();

        BorderPane bp = new BorderPane();

        HBox actors = new HBox();

        t.getContentAsObject().getActors().forEach(actorUUID -> {
            fal.findByReference(actorUUID).ifPresent(note -> {
                Actor actualActor = ((ActorNote) note).getContentAsObject();
                VBox actorBox = new VBox();
                Label actorName = new Label();
                ImageView actorPortrait = new ImageView();
                actorPortrait.setFitHeight(200);
                actorPortrait.setPreserveRatio(true);

                actualActor.getItems().stream().filter(ai -> ai.getLabel().equals(fal.getSetting(NAME_FIELD_NAME).get())).findFirst().ifPresent(name ->
                        actorName.setText(name.getContent()));
                actualActor.getItems().stream().filter(ai -> ai.getLabel().equals(fal.getSetting(PORTRAIT_FIELD_NAME).get())).findFirst().ifPresent(portrait ->
                        fal.getImageFromString(portrait.getContent()).ifPresent(image ->
                                actorPortrait.setImage(image.getValue())));

                actorBox.getChildren().addAll(actorName, actorPortrait);
                actorBox.setMaxWidth(200);
                actorBox.setMaxHeight(200);
                actors.getChildren().add(actorBox);

                if (requester != null) {
                    actorPortrait.onMouseClickedProperty().set(e -> {
                        if (e.getButton() == MouseButton.SECONDARY) {
                            requester.call(note.getReference());
                        }
                    });
                }
            });
        });

        bp.setTop(actors);

        Label location = new Label();

        fal.findByReference(t.getContentAsObject().getLocation()).ifPresent(loc -> {
            String locationName = ((LocationNote) loc).getContentAsObject().getName();
            String canonicalLocationName = ((LocationNote) loc).getContentAsObject().getCanonicalName();
            if (canonicalLocationName != null && !canonicalLocationName.isEmpty()) {
                location.setText(locationName + " (AKA " + canonicalLocationName + " )");
            } else {
                location.setText(locationName);
            }
        });


        TextFlow shortScript = new TextFlow();

        shortScript.getChildren().addAll(Arrays.stream(t.getContentAsObject().getShortDescription().split("")).map(Text::new).toList());
        shortScript.setPadding(new Insets(0, 0, 0, 20));


        TextFlow script = new TextFlow();

        script.getChildren().addAll(Arrays.stream(t.getContentAsObject().getLongDescription().split("")).map(Text::new).toList());
        script.setPadding(new Insets(0, 0, 0, 20));

        VBox scriptBox = new VBox();
        scriptBox.getChildren().addAll(location, shortScript, new Label("Notizen"), script);
        ScrollPane scroll = new ScrollPane(scriptBox);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        bp.setCenter(scroll);

        return bp;
    }

    @Override
    public Note createNewNote() {
        return new SceneNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerViewer(this);
        registry.registerType("scene", SceneNote.class);
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;
    }
}
