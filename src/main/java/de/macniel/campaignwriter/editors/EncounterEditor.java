package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.ActorNoteRenderer;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Registry;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import de.macniel.campaignwriter.types.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

import java.util.List;

public class EncounterEditor extends EditorPlugin<EncounterNote> {

    CombatantNote actualNote;
    private ListView<Note> notesLister;
    private EncounterNote activeNote;
    private TextField encounterNameProp;
    private ComboBox<LocationNote> encounterLocationProp;
    private TextArea circumstancesProp;
    private TextField encounterDifficultyProp;
    private ComboBox<ActorNote> addCombatant = new ComboBox<>();
    private FlowPane combatantsPropLine;
    private VBox scroller;

    @Override
    public String defineHandler() {
        return "encounter/encounter";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();

        Label addCombatantLabel = new Label("Teilnehmer");
        addCombatant.setCellFactory(noteListView -> new ActorNoteRenderer());

        t.getItems().addAll(addCombatantLabel, addCombatant);

        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {

        scroller = new VBox();

        HBox encounterNamePropLine = new HBox();

        encounterNameProp = new TextField();

        encounterNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            activeNote.getContentAsObject().setEncounterName(newValue);
            updateScroller();
        });

        Label encounterNamePropLineLabel = new Label("%EncounterName");
        encounterNamePropLineLabel.setPrefWidth(120);

        encounterNamePropLine.getChildren().addAll(encounterNamePropLineLabel, encounterNameProp);

        HBox encounterLocationPropLine = new HBox();

        Label encounterLocationPropLineLabel = new Label("%Location");
        encounterLocationPropLineLabel.setPrefWidth(120);

        encounterLocationProp = new ComboBox<>();
        encounterLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setEncounterLocation(newValue.getReference());
            }
        });
        encounterLocationPropLine.getChildren().addAll(encounterLocationPropLineLabel, encounterLocationProp);

        HBox circumstancesPropLine = new HBox();

        Label circumstancesPropLineLabel = new Label("%Circumstance");
        circumstancesPropLineLabel.setPrefWidth(120);

        circumstancesProp = new TextArea();
        circumstancesProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setCircumstances(newValue);
            }
        });

        circumstancesPropLine.getChildren().addAll(circumstancesPropLineLabel, circumstancesProp);

        HBox encounterDifficultyPropLine = new HBox();

        Label encounterDifficultyPropLineLabel = new Label("%Difficulty");
        encounterDifficultyPropLineLabel.setPrefWidth(120);


        encounterDifficultyProp  =new TextField();
        encounterDifficultyProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setEncounterDifficulty(newValue);
            }
        });

        encounterDifficultyPropLine.getChildren().addAll(encounterDifficultyPropLineLabel, encounterDifficultyProp);

        addCombatant.getSelectionModel().selectedItemProperty().addListener( (observableValue, note, newValue) -> {
            if (newValue != null) {
                CombatantNote cn = new CombatantNote();
                cn.setContentFromObject(Combatant.fromActor(newValue.getReference()));

                activeNote.getContentAsObject().getCombatants().add(cn);
                updateCombatantBox();
                addCombatant.getSelectionModel().clearSelection();
            }
        });

        combatantsPropLine = new FlowPane();

        combatantsPropLine.getStyleClass().add("combatants");

        ScrollPane scrollPane = new ScrollPane();

        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            combatantsPropLine.setMaxWidth(newValue.doubleValue());
            combatantsPropLine.setPrefWidth(newValue.doubleValue());
        });
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        combatantsPropLine.setMaxWidth(Double.POSITIVE_INFINITY);
        combatantsPropLine.setPrefWidth(Double.POSITIVE_INFINITY);

        scrollPane.setContent(combatantsPropLine);

        scroller.getChildren().addAll(encounterNamePropLine, circumstancesPropLine, encounterDifficultyPropLine, scrollPane);

        return scroller;
    }

    void updateCombatantBox() {

        combatantsPropLine.getChildren().clear();
        activeNote.getContentAsObject().getCombatants().forEach(combatant -> {

            VBox box = new VBox();
            box.getStyleClass().add("combatant");
            box.getStyleClass().add("combatant--" + combatant.getContentAsObject().getTeamColor().toString().toLowerCase());
            box.setPrefWidth(200);
            box.setMinWidth(200);
            box.setMinHeight(200);

            Label combatantName = new Label();
            combatantName.setMaxWidth(200);
            combatantName.setMinWidth(200);
            combatantName.setAlignment(Pos.CENTER);


            ImageView combatantPortrait = new ImageView();

            combatantPortrait.setFitHeight(200);
            combatantPortrait.setFitWidth(200);

            combatantPortrait.setPreserveRatio(true);

            HBox center = new HBox(combatantPortrait);
            VBox.setVgrow(center, Priority.NEVER);
            center.getStyleClass().add("combatant__image");
            center.setAlignment(Pos.CENTER);

            combatantPortrait.onMouseClickedProperty().set(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    activeNote.getContentAsObject().getCombatants().remove(combatant);
                    updateCombatantBox();
                } else if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {

                    // FIXME: find combatant viewerPlugin and use it to display selected combatant
                    System.out.println("encounter/" + combatant.getType());
                    Registry.getInstance().getViewerByFullName("encounter/" + combatant.getType()).ifPresent(viewer -> {
                        final Stage wnd = new Stage();
                        AnchorPane b = new AnchorPane();

                        b.getChildren().add(viewer.getStandaloneVersion(combatant, wnd));
                        Scene popoutContent = new Scene(b, 300, 300);
                        wnd.setScene(popoutContent);
                        wnd.show();
                    });


                }
                mouseEvent.consume();
            });

            // FIXME: dynamic field in encounterdef
            combatant.getContentAsObject().getItems().stream().filter(actorNoteItem ->
                    "Name".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( text -> {
                combatantName.setText(text.getContent());
            });

            // FIXME: dynamic field in encounterdef
            combatant.getContentAsObject().getItems().stream().filter(actorNoteItem ->
                    "Portrait".equals(actorNoteItem.getLabel())
            ).findFirst().flatMap(text -> FileAccessLayer.getInstance().getImageFromString(text.getContent())).ifPresent(entry -> {
                combatantPortrait.setImage(entry.getValue());
            });

            HBox hitpoints = new HBox();
            hitpoints.getStyleClass().add("combatant-resource");

            TextField currentHP = new TextField();
            Label sep = new Label("/");
            Label maxHP = new Label();
            maxHP.setMaxWidth(Double.POSITIVE_INFINITY);
            HBox.setHgrow(currentHP, Priority.SOMETIMES);
            HBox.setHgrow(maxHP, Priority.SOMETIMES);

            // FIXME: dynamic field in encounterdef, also allow to display multiple resources
            combatant.getContentAsObject().getItems().stream().filter(actorNoteItem ->
                    "Hitpoints".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( resource -> {
                currentHP.setText(""+resource.getValue());
                maxHP.setText(""+resource.getMax());


                currentHP.onKeyReleasedProperty().set( event -> {
                    if (event.getCode() == KeyCode.ENTER) {
                        if (currentHP.getText().indexOf("-") >= 1) {
                            int opr1 = Integer.parseInt(currentHP.getText().split("-")[0]);
                            int opr2 = Integer.parseInt(currentHP.getText().split("-")[1]);
                            currentHP.setText(String.valueOf((opr1 - opr2)));

                        } else if (currentHP.getText().indexOf("+") >= 1) {
                            int opr1 = Integer.parseInt(currentHP.getText().split("\\+")[0]);
                            int opr2 = Integer.parseInt(currentHP.getText().split("\\+")[1]);
                            currentHP.setText(String.valueOf((opr1 + opr2)));

                        }
                        resource.setValue(Integer.valueOf(currentHP.getText()));
                    }
                });
            });

            hitpoints.getChildren().addAll(currentHP, sep, maxHP);

            ColorPicker cp = new ColorPicker();
            cp.setValue(combatant.getContentAsObject().getTeamColor());
            cp.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != newValue) {
                    combatant.getContentAsObject().setTeamColor(newValue);
                }
            });

            enableDragandDrop(activeNote.getContentAsObject().getCombatants(), combatant, combatantPortrait);
            box.getChildren().addAll(combatantName, center, hitpoints, cp);

            combatantsPropLine.getChildren().addAll(box);
        });
    }

    CombatantNote dragItem;
    int dragPosition = 0;

    void enableDragandDrop(List<CombatantNote> parentList, CombatantNote dragElement, Node dragHandler) {


        dragHandler.onDragOverProperty().set(e -> {
            dragPosition = parentList.indexOf(dragElement);
            e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });

        dragHandler.onDragExitedProperty().set(e -> {

        });

        dragHandler.onDragEnteredProperty().set(e -> {
            dragPosition = parentList.indexOf(dragElement);
            e.acceptTransferModes(TransferMode.MOVE);
            e.consume();
        });

        dragHandler.onDragDroppedProperty().set(e -> {
            if (dragItem != null) {
                parentList.remove(dragItem);
                parentList.add(dragPosition, dragItem);
                updateCombatantBox();

            }
            e.setDropCompleted(true);
            e.consume();
        });


        dragHandler.onDragDetectedProperty().set(e -> {
            dragItem = dragElement;
            dragPosition = parentList.indexOf(dragElement);
            javafx.scene.input.Dragboard db = dragHandler.startDragAndDrop(TransferMode.ANY);
            ClipboardContent c = new ClipboardContent();
            c.putString("accepted");
            db.setContent(c);

            e.consume();
        });
    }

    private void updateScroller() {
        if (activeNote != null) {
            scroller.setVisible(true);
            encounterNameProp.setText(activeNote.getContentAsObject().getEncounterName());
            ObservableList<LocationNote> items = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.getType().equals("location")).map(note -> (LocationNote) note).toList());
            encounterLocationProp.setItems(items);


            FileAccessLayer.getInstance().findByReference(activeNote.getContentAsObject().getEncounterLocation()).ifPresent(selectedNote -> {
                encounterLocationProp.getSelectionModel().select((LocationNote) selectedNote);
            });
            circumstancesProp.setText(activeNote.getContentAsObject().getCircumstances());
            encounterDifficultyProp.setText(activeNote.getContentAsObject().getEncounterDifficulty());

            ObservableList<ActorNote> actorItems = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.getType().equals("actor")).map(note -> (ActorNote) note).toList());
            actorItems.add(0, null);
            addCombatant.setItems(actorItems);
            addCombatant.getSelectionModel().clearSelection();

            updateCombatantBox();
        } else {
            scroller.setVisible(false);
        }
    }


    @Override
    public Callback<Boolean, EncounterNote> defineSaveCallback() {
        return p -> activeNote;
    }

    @Override
    public Callback<EncounterNote, Boolean> defineLoadCallback() {
        return param -> {
            activeNote = param;
            updateScroller();
            return true;
        };
    }

    private Callback<ActorNoteItem, Boolean> changeCallback;

    public void setChangeCallback(Callback<ActorNoteItem, Boolean> changeCallback) {
        this.changeCallback = changeCallback;
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }

    @Override
    public EncounterNote createNewNote() {
        return new EncounterNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
        registry.registerType("encounter", EncounterNote.class);
    }
}
