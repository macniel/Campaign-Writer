package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.fxmisc.richtext.InlineCssTextArea;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;

public class ActorEditor implements EditorPlugin {

    Gson gsonParser;

    ActorNoteDefinition notesStructure;
    private VBox editor;


    @Override
    public NoteType defineHandler() {
        return NoteType.ACTOR_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {

    }

    @Override
    public Node defineEditor() {

        editor = new VBox();

        refreshEditor();

        return editor;
    }

    int dragposition;
    ActorNoteItem dragelement;

    private HBox renderItem(ActorNoteItem item) {
        HBox line = new HBox();
        Button removeLine = new Button("", new FontIcon("icm-bin"));
        removeLine.onActionProperty().set(e -> {
            notesStructure.items.remove(item);
            refreshEditor();
        });

        switch(item.type) {
            case TEXT -> {
                TextField label = new TextField();
                label.setPrefWidth(120);
                label.setText(item.label);
                TextArea texteditor = new TextArea(item.content);
                texteditor.setWrapText(true);
                texteditor.setPrefRowCount(3);

                label.textProperty().addListener( (editor, oldText, newText) -> {
                    item.label = newText;
                });

                texteditor.textProperty().addListener( (editor, oldText, newText) -> {

                    item.content = newText;
                });
                line.getChildren().add(label);
                line.getChildren().add(texteditor);
                line.getChildren().add(removeLine);
            }
            case STRING -> {
                TextField label = new TextField();
                label.setPrefWidth(120);
                label.setText(item.label);
                TextField texteditor = new TextField(item.content);

                label.textProperty().addListener( (editor, oldText, newText) -> {
                    item.label = newText;
                });

                texteditor.textProperty().addListener( (editor, oldText, newText) -> {

                    item.content = newText;
                });

                line.getChildren().add(label);
                line.getChildren().add(texteditor);
                line.getChildren().add(removeLine);
            }
            case IMAGE -> {
                TextField label = new TextField();
                label.setPrefWidth(120);
                label.setText(item.label);
                ImageView v = new ImageView();
                v.setPreserveRatio(true);
                v.setFitWidth(250);
                v.setFitHeight(250);

                if (item.content != null) {
                    v.setImage(FileAccessLayer.getImageFromString(item.content));
                }

                Button selectFileButton = new Button("", new FontIcon("icm-link"));

                label.textProperty().addListener( (editor, oldText, newText) -> {
                    item.label = newText;
                });

                selectFileButton.onActionProperty().set(e -> {
                    FileChooser chooser = new FileChooser();
                    File selectedFile = chooser.showOpenDialog(null);
                    if (selectedFile != null) {
                        item.content = selectedFile.getAbsolutePath();
                        v.setImage(FileAccessLayer.getImageFromString(item.content));
                    }
                });


                line.getChildren().add(label);
                line.getChildren().add(v);
                line.getChildren().add(selectFileButton);
                line.getChildren().add(removeLine);
            }
            case HEADER -> {
                VBox label = new VBox();
                label.setPrefWidth(120);

                TextField content = new TextField();
                content.setAlignment(Pos.TOP_CENTER);
                content.setText(item.content);

                content.textProperty().addListener( (editor, oldText, newText) -> {
                    item.content = newText;
                });


                line.getChildren().add(label);
                line.getChildren().add(content);
                line.getChildren().add(removeLine);
            }
            case RESOURCE -> {
                System.out.println("RESOURCE");
                TextField label = new TextField();
                label.setPrefWidth(120);
                label.setText(item.label);
                TextField value = new TextField(String.valueOf(item.value));
                TextField maxValue = new TextField(String.valueOf(item.max));

                label.textProperty().addListener( (editor, oldText, newText) -> {
                    item.label = newText;
                });

                value.textProperty().addListener( (editor, oldText, newText) -> {
                    item.value = Integer.valueOf(newText);
                });
                maxValue.textProperty().addListener( (editor, oldText, newText) -> {
                    item.max = Integer.valueOf(newText);
                });

                line.getChildren().add(label);
                line.getChildren().add(value);
                line.getChildren().add(new Label(" / "));
                line.getChildren().add(maxValue);
                line.getChildren().add(removeLine);
            }
        }
        return line;
    }

    private void refreshEditor() {

        ObservableList<ActorNoteItem.ActorNoteItemType> types = FXCollections.observableArrayList();
        types.add(ActorNoteItem.ActorNoteItemType.HEADER);
        types.add(ActorNoteItem.ActorNoteItemType.STRING);
        types.add(ActorNoteItem.ActorNoteItemType.TEXT);
        types.add(ActorNoteItem.ActorNoteItemType.IMAGE);
        types.add(ActorNoteItem.ActorNoteItemType.RESOURCE);
        editor.getChildren().clear();

        if (notesStructure != null) {
            System.out.println("Rendering " + notesStructure.items.size() + " elements");


            notesStructure.items.forEach(item -> {
                System.out.println("rendering item with label " + item.label);
                HBox line = renderItem(item);
                FontIcon fi = new FontIcon("icm-page-break");
                fi.setIconSize(20);

                Label dragButton = new Label("", fi);
                dragButton.setPrefWidth(24);
                dragButton.setPrefHeight(24);


                dragButton.onDragOverProperty().set(e -> {
                    fi.setIconColor(Color.BLUE);
                    System.out.println("New Dragposition " + dragposition);
                    dragposition = editor.getChildren().indexOf(line);
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });

                dragButton.onDragExitedProperty().set(e -> {
                    fi.setIconColor(Color.BLACK);
                });

                dragButton.onDragEnteredProperty().set(e -> {
                    fi.setIconColor(Color.BLUE);
                    System.out.println("New Dragposition " + dragposition);
                    dragposition = editor.getChildren().indexOf(line);
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });

                dragButton.onDragDroppedProperty().set(e -> {
                    if (dragelement != null) {
                        notesStructure.items.remove(dragelement);
                        notesStructure.items.add(dragposition, dragelement);
                        refreshEditor();
                    }
                    e.setDropCompleted(true);
                    e.consume();
                });


                dragButton.onDragDetectedProperty().set(e -> {

                    System.out.println("Drag detected");
                    dragelement = item;
                    dragposition = editor.getChildren().indexOf(line);
                    Dragboard db = dragButton.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent c = new ClipboardContent();
                    c.putString("accepted");
                    db.setContent(c);

                    e.consume();
                });

                line.getChildren().add(0, dragButton);
                editor.getChildren().add(line);
            });
        }
        HBox newLine = new HBox();
        ComboBox<ActorNoteItem.ActorNoteItemType> newType = new ComboBox<>();
        newType.setItems(types);
        newType.setPromptText("Neue");
        newType.setPrefWidth(120);
        newType.onActionProperty().set(e -> {
                System.out.println("adding a new line of type " + newType.getValue());
                if (notesStructure == null) {
                    notesStructure = new ActorNoteDefinition();
                }
                if (notesStructure.items == null) {
                    notesStructure.items = new ArrayList<>();
                }
                ActorNoteItem added = new ActorNoteItem();
                added.type = newType.getValue();
                notesStructure.items.add(added);
                refreshEditor();

        });
        VBox v = new VBox();
        v.fillWidthProperty().set(true);
        newLine.getChildren().add(newType);
        newLine.getChildren().add(v);
        editor.getChildren().add(newLine);
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (gsonParser == null) {
                    gsonParser = new Gson();
                }
                note.content = gsonParser.toJson(notesStructure);
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (gsonParser == null) {
                    gsonParser = new Gson();
                }
                notesStructure = gsonParser.fromJson(note.content, ActorNoteDefinition.class);
                refreshEditor();
                return true;
            }
        };
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}
