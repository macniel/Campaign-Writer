package de.macniel.campaignwriter.views;

import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.CampaignFileInterface;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import de.macniel.campaignwriter.types.Combatant;
import de.macniel.campaignwriter.types.Encounter;
import de.macniel.campaignwriter.types.EncounterNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class EncounterView extends ViewerPlugin {
    private final ResourceBundle i18n;
    @FXML
    public TextField encounterNameProp;
    @FXML
    public ComboBox<Note> encounterLocationProp;
    @FXML
    public TextArea circumstancesProp;
    @FXML
    public TextField encounterDifficultyProp;
    @FXML
    public FlowPane combatantsPropLine;
    @FXML
    public ComboBox<Note> addCombatant;
    @FXML
    private ListView<Encounter> notesLister;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private VBox scroller;

    private Callback<UUID, Note> requester;

    Encounter activeNote;

    List<Encounter> encounters;

    @Override
    public String getPathToFxmlDefinition() {
        return "encounter-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return i18n.getString("EncounterViewMenuItem");
    }

    @Override
    public void requestLoad(CampaignFileInterface items) {
        encounters = items.getNotes().stream().filter(note -> note.getType().endsWith("encounter")).map(note -> ((EncounterNote) note).getContent()).toList();
        notesLister.setItems(FXCollections.observableArrayList(encounters));

        String lastLoadedNote = FileAccessLayer.getInstance().getSetting("lastNote");
        if (lastLoadedNote != null) {
            FileAccessLayer.getInstance().getAllNotes().stream().filter(note -> note.getType().endsWith("encounter")).filter(n -> n.getReference().toString().equals(lastLoadedNote)).findFirst().ifPresent(note -> {
                activeNote = ((EncounterNote) note).getContent();
                notesLister.getSelectionModel().select(activeNote);
            });
        }
    }

    @Override
    public void requestSave() {

    }

    public static String getLocalizationBase() {
        return "i18n.encounters";
    }

    @Override
    public String defineViewerHandlerPrefix() {
        return "encounter";
    }

    public EncounterView() {
        this.i18n = ResourceBundle.getBundle(getLocalizationBase());
    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }

    @FXML
    public void initialize() {

        notesLister.getSelectionModel().selectedItemProperty().addListener( (observableValue, encounterNote, newNote) -> {
            if (newNote != null) {
                activeNote = newNote;
                FileAccessLayer.getInstance().updateSetting("lastNote", newNote.getReference().toString());
                updateScroller();
            }
        });

        encounterNameProp.textProperty().addListener((observableValue, s, newValue) -> {
            activeNote.setEncounterName(newValue);
            updateScroller();
        });

        encounterLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newValue) -> {
            if (newValue != null) {
                activeNote.setEncounterLocation(newValue.reference);
            }
        });

        circumstancesProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.setCircumstances(newValue);
            }
        });

        encounterDifficultyProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.setEncounterDifficulty(newValue);
            }
        });

        addCombatant.getSelectionModel().selectedItemProperty().addListener( (observableValue, note, newValue) -> {
            if (newValue != null) {
                activeNote.getCombatants().add(Combatant.fromActor(newValue.reference));
                updateCombatantBox();
                addCombatant.getSelectionModel().clearSelection();
            }
        });

        combatantsPropLine.getStyleClass().add("combatants");

        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> {
            combatantsPropLine.setMaxWidth(newValue.doubleValue());
            combatantsPropLine.setPrefWidth(newValue.doubleValue());
        });
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        combatantsPropLine.setMaxWidth(Double.POSITIVE_INFINITY);
        combatantsPropLine.setPrefWidth(Double.POSITIVE_INFINITY);

    }

    void updateCombatantBox() {

        combatantsPropLine.getChildren().clear();
        activeNote.getCombatants().forEach(combatant -> {

            VBox box = new VBox();
            box.getStyleClass().add("combatant");
            box.getStyleClass().add("combatant--" + combatant.teamColor.toString().toLowerCase());
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
                    activeNote.getCombatants().remove(combatant);
                    updateCombatantBox();
                } else if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    /*CombatantViewer cv = new CombatantViewer();
                    final Stage wnd = new Stage();
                    AnchorPane b = new AnchorPane();
                    cv.setChangeCallback(new Callback<ActorNoteItem,Boolean>() {

                        @Override
                        public Boolean call(ActorNoteItem param) {
                            updateCombatantBox();
                            return true;
                        }
                        
                    });
                    b.getChildren().add(cv.renderNoteStandalone(combatant, wnd));
                    Scene popoutContent = new Scene(b, 300, 300);
                    wnd.setScene(popoutContent);
                    wnd.show();*/
                }
                mouseEvent.consume();
            });

            // FIXME: dynamic field in encounterdef
            combatant.items.stream().filter(actorNoteItem ->
                    "Name".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( text -> {
                combatantName.setText(text.getContent());
            });

            // FIXME: dynamic field in encounterdef
            combatant.items.stream().filter(actorNoteItem ->
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
            combatant.items.stream().filter(actorNoteItem ->
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
            cp.setValue(combatant.teamColor);
            cp.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != newValue) {
                    combatant.teamColor = newValue;
                }
            });

            enableDragandDrop(activeNote.getCombatants(), combatant, combatantPortrait);
            box.getChildren().addAll(combatantName, center, hitpoints, cp);

            combatantsPropLine.getChildren().addAll(box);
        });
    }

    Combatant dragItem;
    int dragPosition = 0;

    void enableDragandDrop(List<Combatant> parentList, Combatant dragElement, Node dragHandler) {

        
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

    // TODO: move to Encounter Editor
    private void updateScroller() {
        if (activeNote != null) {
            scroller.setVisible(true);
            encounterNameProp.setText(activeNote.getEncounterName());
            ObservableList<Note> items = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.getType().equals("location")).toList());
            encounterLocationProp.setItems(items);


            FileAccessLayer.getInstance().findByReference(activeNote.getEncounterLocation()).ifPresent(selectedNote -> {
                encounterLocationProp.getSelectionModel().select(selectedNote);
            });
            circumstancesProp.setText(activeNote.getCircumstances());
            encounterDifficultyProp.setText(activeNote.getEncounterDifficulty());

            ObservableList<Note> actorItems = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.getType().equals("actor")).toList());
            actorItems.add(0, null);
            addCombatant.setItems(actorItems);
            addCombatant.getSelectionModel().clearSelection();

            updateCombatantBox();
        } else {
            scroller.setVisible(false);
        }
    }

    public void newEncounter(ActionEvent actionEvent) {
        Encounter n = new Encounter();
        n.setEncounterName(i18n.getString("UntitledEncounter"));
        encounters.add(n);
        notesLister.setItems(FXCollections.observableArrayList(encounters));
        activeNote = n;
        updateScroller();
    }

    public void deleteEncounter(ActionEvent actionEvent) {
        if (activeNote != null && encounters.contains(activeNote)) {
            encounters.remove(activeNote);
            activeNote = null;
            notesLister.getSelectionModel().clearSelection();
            updateScroller();
        }
    }

    public void beginEncounter(ActionEvent actionEvent) {
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerViewer(this);
    }
}

