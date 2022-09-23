package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.CampaignFile;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.Combatant;
import de.macniel.campaignwriter.editors.EncounterNote;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EncounterView  implements ViewInterface {
    @FXML
    public TextField encounterNameProp;
    @FXML
    public ComboBox<Note> encounterLocationProp;
    @FXML
    public TextArea circumstancesProp;
    @FXML
    public TextField encounterDifficultyProp;
    @FXML
    public HBox combatantsPropLine;
    @FXML
    public ComboBox<Note> addCombatant;
    @FXML
    private ListView<EncounterNote> notesLister;

    @FXML
    private VBox scroller;

    private Callback<UUID, Note> requester;

    EncounterNote activeNote;

    List<EncounterNote> encounters;

    @Override
    public String getPathToFxmlDefinition() {
        return "encounter-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return "Begegnung";
    }

    @Override
    public void requestLoad(CampaignFile items) {
        encounters = items.encounterNotes;
        notesLister.setItems(FXCollections.observableArrayList(encounters));
    }

    @Override
    public void requestSave() {

    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }
    
    @FXML
    public void initialize() {

        notesLister.getSelectionModel().selectedItemProperty().addListener( (observableValue, encounterNote, newNote) -> {
            activeNote = newNote;
            updateScroller();
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
                System.out.println("add new Combatant from " + newValue.label);
                activeNote.getCombatants().add(Combatant.fromActor(newValue.reference));
                updateCombatantBox();
                addCombatant.getSelectionModel().clearSelection();
            }
        });

    }

    void updateCombatantBox() {

        combatantsPropLine.getChildren().clear();
        activeNote.getCombatants().forEach(combatant -> {

            VBox box = new VBox();
            box.setPrefWidth(200);
            box.setPrefHeight(200);
            box.setMinWidth(200);
            box.setMinHeight(200);

            Label combatantName = new Label();
            ImageView combatantPortrait = new ImageView();
            combatantPortrait.setFitHeight(200);
            combatantPortrait.setFitWidth(200);

            combatantPortrait.setPreserveRatio(true);
            HBox.setHgrow(combatantPortrait, Priority.ALWAYS);
            VBox.setVgrow(combatantPortrait, Priority.ALWAYS);

            combatantPortrait.onMouseClickedProperty().set(mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    activeNote.getCombatants().remove(combatant);
                    updateCombatantBox();
                } else if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                    System.out.println("requesting details of note " + combatantName);
                }
                mouseEvent.consume();
            });

            combatant.items.stream().filter(actorNoteItem ->
                    "Name".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( text -> {
                System.out.println("Found Name ");
                combatantName.setText(text.getContent());
            });

            combatant.items.stream().filter(actorNoteItem ->
                    "Portrait".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( text -> {
                Map.Entry<String, Image> entry = FileAccessLayer.getInstance().getImageFromString(text.getContent());
                System.out.println("Found portrait image " + entry.getKey());
                combatantPortrait.setImage(entry.getValue());
            });

            HBox hitpoints = new HBox();

            TextField currentHP = new TextField();
            Label sep = new Label("/");
            Label maxHP = new Label();
            maxHP.setMaxWidth(Double.POSITIVE_INFINITY);
            HBox.setHgrow(currentHP, Priority.SOMETIMES);
            HBox.setHgrow(maxHP, Priority.SOMETIMES);

            combatant.items.stream().filter(actorNoteItem ->
                    "Hitpoints".equals(actorNoteItem.getLabel())
            ).findFirst().ifPresent( resource -> {
                System.out.println("Found Hitpoints ");
                currentHP.setText(""+resource.getValue());
                maxHP.setText(""+resource.getMax());

                currentHP.textProperty().addListener((observableValue, s, t1) -> {
                    resource.setValue(Integer.valueOf(t1));
                });
            });

            hitpoints.getChildren().addAll(currentHP, sep, maxHP);
            box.getChildren().addAll(combatantName, combatantPortrait, hitpoints);
            combatantsPropLine.getChildren().addAll(box);
        });
    }

    private void updateScroller() {
        if (activeNote != null) {
            scroller.setVisible(true);
            encounterNameProp.setText(activeNote.getEncounterName());
            ObservableList<Note> items = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.type == NoteType.LOCATION_NOTE).toList());
            encounterLocationProp.setItems(items);


            FileAccessLayer.getInstance().findByReference(activeNote.getEncounterLocation()).ifPresent(selectedNote -> {
                encounterLocationProp.getSelectionModel().select(selectedNote);
            });
            circumstancesProp.setText(activeNote.getCircumstances());
            encounterDifficultyProp.setText(activeNote.getEncounterDifficulty());

            ObservableList<Note> actorItems = FXCollections.observableArrayList(FileAccessLayer.getInstance().getAllNotes().stream().filter(note  -> note.type == NoteType.ACTOR_NOTE).toList());
            actorItems.add(0, null);
            addCombatant.setItems(actorItems);
            addCombatant.getSelectionModel().clearSelection();

            updateCombatantBox();
        } else {
            scroller.setVisible(false);
        }
    }

    public void newEncounter(ActionEvent actionEvent) {
        EncounterNote n = new EncounterNote();
        n.setEncounterName("Neuer Encounter");
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
}

