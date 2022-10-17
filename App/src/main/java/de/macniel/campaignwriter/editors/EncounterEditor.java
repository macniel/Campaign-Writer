package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.*;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static de.macniel.campaignwriter.FileAccessLayer.*;

public class EncounterEditor extends EditorPlugin<EncounterNote> implements ViewerPlugin<EncounterNote>, Configurable {

    private final ResourceBundle i18n;
    private final ComboBox<ActorNote> addCombatant = new ComboBox<>();
    CombatantNote actualNote;
    CombatantNote dragItem;
    int dragPosition = 0;
    private ListView<Note> notesLister;
    private EncounterNote activeNote;
    private TextField encounterNameProp;
    private ComboBox<LocationNote> encounterLocationProp;
    private TextArea circumstancesProp;
    private TextField encounterDifficultyProp;
    private FlowPane combatantsPropLine;
    private VBox scroller;
    private Callback<ActorNoteItem, Boolean> changeCallback;

    public EncounterEditor() {
        this.i18n = ResourceBundle.getBundle("i18n.encounters");
    }

    @Override
    public String defineHandler() {
        return "encounter/encounter";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();

        Label addCombatantLabel = new Label(i18n.getString("AddCombatant"));
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

        Label encounterNamePropLineLabel = new Label(i18n.getString("EncounterName"));
        encounterNamePropLineLabel.setPrefWidth(120);

        encounterNamePropLine.getChildren().addAll(encounterNamePropLineLabel, encounterNameProp);

        HBox encounterLocationPropLine = new HBox();

        Label encounterLocationPropLineLabel = new Label(i18n.getString("Location"));
        encounterLocationPropLineLabel.setPrefWidth(120);

        encounterLocationProp = new ComboBox<>();
        encounterLocationProp.setCellFactory(listView -> new LocationNoteRenderer());
        encounterLocationProp.setButtonCell(new LocationNoteRenderer());
        encounterLocationProp.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setEncounterLocation(newValue.getReference());
            }
        });
        encounterLocationPropLine.getChildren().addAll(encounterLocationPropLineLabel, encounterLocationProp);

        HBox circumstancesPropLine = new HBox();

        Label circumstancesPropLineLabel = new Label(i18n.getString("Circumstance"));
        circumstancesPropLineLabel.setPrefWidth(120);

        circumstancesProp = new TextArea();
        circumstancesProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setCircumstances(newValue);
            }
        });

        circumstancesPropLine.getChildren().addAll(circumstancesPropLineLabel, circumstancesProp);

        HBox encounterDifficultyPropLine = new HBox();

        Label encounterDifficultyPropLineLabel = new Label(i18n.getString("Difficulty"));
        encounterDifficultyPropLineLabel.setPrefWidth(120);


        encounterDifficultyProp = new TextField();
        encounterDifficultyProp.textProperty().addListener((observableValue, s, newValue) -> {
            if (newValue != null) {
                activeNote.getContentAsObject().setEncounterDifficulty(newValue);
            }
        });

        encounterDifficultyPropLine.getChildren().addAll(encounterDifficultyPropLineLabel, encounterDifficultyProp);

        addCombatant.getSelectionModel().selectedItemProperty().addListener((observableValue, note, newValue) -> {
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

        scroller.getChildren().addAll(encounterNamePropLine, circumstancesPropLine, encounterDifficultyPropLine, encounterLocationPropLine, scrollPane);

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
                    FileAccessLayer.getInstance().getSetting(NAME_FIELD_NAME).get().equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent(text -> {
                combatantName.setText(text.getContent());
            });

            // FIXME: dynamic field in encounterdef
            combatant.getContentAsObject().getItems().stream().filter(actorNoteItem ->
                    FileAccessLayer.getInstance().getSetting(PORTRAIT_FIELD_NAME).get().equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent(ani -> {

                new FileAccessLayerFactory().get().getImageFromString(ani.getContent()).ifPresent(entry -> {
                    ani.setContent(entry.getKey());
                    combatantPortrait.setImage(entry.getValue());
                });
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
                    FileAccessLayer.getInstance().getSetting(HITPOINTS_FIELD_NAME).get().equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent(resource -> {

                currentHP.setText("" + (resource.getValue() == null ? 0 : resource.getValue()));
                maxHP.setText("" + (resource.getMax() == null ? 0 : resource.getMax()));


                currentHP.onKeyReleasedProperty().set(event -> {
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
            ObservableList<LocationNote> items = FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().equals("location")).map(note -> (LocationNote) note).toList());
            encounterLocationProp.setItems(items);


            new FileAccessLayerFactory().get().findByReference(activeNote.getContentAsObject().getEncounterLocation()).ifPresent(selectedNote -> {
                encounterLocationProp.getSelectionModel().select((LocationNote) selectedNote);
            });
            circumstancesProp.setText(activeNote.getContentAsObject().getCircumstances());
            encounterDifficultyProp.setText(activeNote.getContentAsObject().getEncounterDifficulty());

            ObservableList<ActorNote> actorItems = FXCollections.observableArrayList(new FileAccessLayerFactory().get().getAllNotes().stream().filter(note -> note.getType().equals("actor")).map(note -> (ActorNote) note).toList());
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

    public void setChangeCallback(Callback<ActorNoteItem, Boolean> changeCallback) {
        this.changeCallback = changeCallback;
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {

    }

    @Override
    public EncounterNote createNewNote() {
        return new EncounterNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerViewer(this);
        registry.registerEditor(this);
        registry.registerType("encounter", EncounterNote.class);
    }


    @Override
    public Node getPreviewVersionOf(EncounterNote t) {
        VBox box = new VBox();

        Label name = new Label(t.getContentAsObject().getEncounterName());
        Label location = new Label();

        new FileAccessLayerFactory().get().findByReference(t.getContentAsObject().getEncounterLocation()).ifPresent(locationNote -> {
            Location l = ((LocationNote) locationNote).getContentAsObject();
            location.setText(l.getName());
        });
        ScrollPane p = new ScrollPane();
        HBox boxes = new HBox();
        boxes.setSpacing(10);
        Registry.getInstance().getViewerFor(t).ifPresent(viewer -> {
            t.getContentAsObject().getCombatants().forEach(combatantNote -> { // FIXME: make it ref!

                VBox combatant = new VBox();
                combatantNote.getContentAsObject().getItems().stream().filter(i -> FileAccessLayer.getInstance().getSetting(NAME_FIELD_NAME).get().equals(i.getLabel())).findFirst().ifPresent(nameProp -> {
                    combatant.getChildren().add(new Label(nameProp.getContent()));
                });
                combatantNote.getContentAsObject().getItems().stream().filter(i -> FileAccessLayer.getInstance().getSetting(PORTRAIT_FIELD_NAME).get().equals(i.getLabel())).findFirst().ifPresent(ani -> {
                    new FileAccessLayerFactory().get().getImageFromString(ani.getContent()).ifPresent(entry -> {
                        ani.setContent(entry.getKey());
                        ImageView image = new ImageView(entry.getValue());
                        image.setFitWidth(80);
                        image.setPreserveRatio(true);
                        image.setFitHeight(80);
                        combatant.getChildren().add(image);
                    });

                });
                combatant.setMaxWidth(80);
                Border b = new Border(new BorderStroke(combatantNote.getContentAsObject().getTeamColor(), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5)));

                combatant.setBorder(b);
                boxes.getChildren().add(combatant);
            });
        });

        p.setContent(boxes);
        box.getChildren().addAll(name, location, p);

        return box;
    }

    @Override
    public Node getStandaloneVersion(EncounterNote t, Stage wnd) {
        BorderPane bp = new BorderPane();

        VBox info = new VBox();
        info.getChildren().add(new Label(t.getContentAsObject().getEncounterName()));
        bp.setTop(info);

        ListView<CombatantNote> combatants = new ListView<>();
        ButtonBar combatantsButtons = new ButtonBar();

        Button sortCombatantsByInitiative = new Button("", new FontIcon("icm-sort-numberic-desc"));
        Button addCombatantStandIn = new Button("", new FontIcon("icm-plus"));

        combatantsButtons.getButtons().addAll(sortCombatantsByInitiative, addCombatantStandIn);

        List<CombatantNote> list = t.getContentAsObject().getCombatants();
        AtomicReference<ObservableList<CombatantNote>> observableList = new AtomicReference<>(FXCollections.observableArrayList(list));

        sortCombatantsByInitiative.onActionProperty().set(e -> {
            combatants.setItems(combatants.getItems().sorted((a, b) ->
                    Integer.parseInt(b.getContentAsObject().initiative) - Integer.parseInt(a.getContentAsObject().initiative)
            ));
        });

        addCombatantStandIn.onActionProperty().set(e -> {
            TextInputDialog newName = new TextInputDialog();
            newName.showAndWait();
            if (newName.getResult() != null) {
                CombatantNote standIn = new CombatantNote();
                standIn.label = newName.getResult();
                ActorNoteItem nameProp = new ActorNoteItem();
                nameProp.setType(ActorNoteItem.ActorNoteItemType.STRING);
                nameProp.setLabel(NAME_FIELD_NAME);
                nameProp.setContent(newName.getResult());
                standIn.getContentAsObject().getItems().add(nameProp);
                ActorNoteItem hitpointsProp = new ActorNoteItem();
                hitpointsProp.setType(ActorNoteItem.ActorNoteItemType.RESOURCE);
                hitpointsProp.setLabel(HITPOINTS_FIELD_NAME);
                standIn.getContentAsObject().getItems().add(hitpointsProp);
                ActorNoteItem portraitProp = new ActorNoteItem();
                portraitProp.setType(ActorNoteItem.ActorNoteItemType.IMAGE);
                portraitProp.setLabel(PORTRAIT_FIELD_NAME);
                standIn.getContentAsObject().getItems().add(portraitProp);
                t.getContentAsObject().getCombatants().add(standIn);

                combatants.setItems(FXCollections.observableArrayList(t.getContentAsObject().getCombatants()));
            }

        });

        BorderPane combatantsPane = new BorderPane();
        combatantsPane.setCenter(combatants);
        combatantsPane.setBottom(combatantsButtons);
        bp.setLeft(combatantsPane);

        combatants.setItems(observableList.get());

        combatants.setCellFactory(l -> new ListCell<>() {
            @Override
            protected void updateItem(CombatantNote item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    HBox box = new HBox();

                    ImageView portrait = new ImageView();
                    portrait.setPreserveRatio(true);


                    item.getContentAsObject().getItems().stream().filter(i -> i.getLabel() != null && i.getLabel().equals(FileAccessLayer.getInstance().getSetting(PORTRAIT_FIELD_NAME).get())).findFirst().ifPresent(ani -> {

                        new FileAccessLayerFactory().get().getImageFromString(ani.getContent()).ifPresent(actualImage -> {
                            ani.setContent(actualImage.getKey());
                            portrait.setImage(actualImage.getValue());
                        });

                    });

                    portrait.setFitWidth(80);
                    portrait.setFitHeight(40);

                    Label initiative = new Label();

                    initiative.setText(item.getContentAsObject().getInitiative());

                    Label name = new Label();
                    item.getContentAsObject().getItems().stream().filter(i -> i.getLabel() != null && i.getLabel().equals(FileAccessLayer.getInstance().getSetting(NAME_FIELD_NAME).get())).findFirst().ifPresentOrElse(combatantName -> {
                        name.setText(combatantName.getContent());
                    }, () -> {
                        name.setText("Unbenannt");
                    });


                    HBox stretcher = new HBox();
                    HBox.setHgrow(stretcher, Priority.ALWAYS);

                    box.getChildren().addAll(portrait, initiative, stretcher, name);

                    box.setMaxHeight(40);

                    setGraphic(box);
                }
            }
        });

        combatants.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

            Registry.getInstance().getViewerFor(newValue).ifPresentOrElse(viewer -> {
                System.out.println("rendering with " + viewer.defineHandler());
                bp.setCenter(new ScrollPane(viewer.getPreviewVersionOf(newValue)));
            }, () -> {
                System.err.println("no viewer registered for type " + newValue.getType());
            });
        });

        return bp;
    }

    @Override
    public String getConfigMenuItem() {
        return "Encounter Editor";
    }

    @Override
    public Consumer<Boolean> startConfigureTask(FileAccessLayerInterface fileAccessLayer, RegistryInterface registry, Tab tab) {
        HashMap<String, String> properties = new HashMap<>();
        properties.put(NAME_FIELD_NAME, NAME_FIELD_NAME);
        properties.put(HITPOINTS_FIELD_NAME, HITPOINTS_FIELD_NAME);
        properties.put(PORTRAIT_FIELD_NAME, PORTRAIT_FIELD_NAME);
        HashMap<String, TextField> fields = new HashMap<>();


        VBox settings = new VBox();
        settings.setSpacing(5);
        settings.setFillWidth(true);
        settings.setPadding(new Insets(10));

        properties.entrySet().forEach(setting -> {

            HBox line = new HBox();
            HBox.setHgrow(line, Priority.ALWAYS);

            Label label = new Label(setting.getKey());
            label.setPrefWidth(120);
            TextField prop = new TextField();
            fileAccessLayer.getSetting(setting.getValue()).ifPresentOrElse(prop::setText, () -> {
                prop.setText(setting.getValue());
            });

            HBox.setHgrow(prop, Priority.ALWAYS);
            fields.put(setting.getKey(), prop);
            line.getChildren().addAll(label, prop);
            settings.getChildren().add(line);
        });

        tab.setContent(settings);


        return aBoolean -> {
            if (aBoolean) {
                fields.forEach((key, value) -> fileAccessLayer.updateSetting(key, value.getText()));
            }
        };
    }
}
