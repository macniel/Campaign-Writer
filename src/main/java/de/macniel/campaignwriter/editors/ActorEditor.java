package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class ActorEditor implements EditorPlugin<ActorNoteDefinition> {

    Gson gsonParser;

    ActorNoteDefinition notesStructure;
    private ScrollPane editor;


    @Override
    public NoteType defineHandler() {
        return NoteType.ACTOR_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {

        t.getItems().clear();

        ToggleGroup viewMode = new ToggleGroup();

        ToggleButton previewMode = new ToggleButton("Preview");
        ToggleButton editMode = new ToggleButton("edit");
        previewMode.setToggleGroup(viewMode);
        editMode.setToggleGroup(viewMode);

        viewMode.selectedToggleProperty().addListener( (observableValue, toggle, newValue) -> {
            if (previewMode.equals(newValue)) {
                editor.setContent(getPreviewVersionOf(notesStructure));
            } else {
                editor.setContent(getEditableVersion());
            }
        });

        viewMode.selectToggle(editMode);

        t.getItems().add(previewMode);
        t.getItems().add(editMode);
        t.setVisible(true);

    }

    @Override
    public Node defineEditor() {

        editor = new ScrollPane();

        editor.setContent(getEditableVersion());

        return editor;
    }

    int dragposition;
    ActorNoteItem dragelement;

    private HBox renderItem(ActorNoteItem item) {
        return renderItem(item, true);
    }

    private HBox renderItem(ActorNoteItem item, boolean editable) {
        HBox line = new HBox();
        Button removeLine = new Button("", new FontIcon("icm-bin"));
        removeLine.onActionProperty().set(e -> {
            notesStructure.items.remove(item);
            getEditableVersion();
        });

        switch(item.type) {
            case TEXT -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextArea texteditor = new TextArea(item.content);
                    texteditor.setWrapText(true);
                    texteditor.setPrefRowCount(3);
                    HBox.setHgrow(texteditor, Priority.ALWAYS);

                    label.textProperty().addListener( (editor, oldText, newText) -> {
                        item.label = newText;
                    });

                    texteditor.textProperty().addListener( (editor, oldText, newText) -> {

                        item.content = newText;
                    });
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextFlow texteditor = new TextFlow();

                    texteditor.getChildren().add(new Text(item.content));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }

            }
            case STRING -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextField texteditor = new TextField(item.content);
                    HBox.setHgrow(texteditor, Priority.ALWAYS);
                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.label = newText;
                    });

                    texteditor.textProperty().addListener((editor, oldText, newText) -> {

                        item.content = newText;
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextFlow texteditor = new TextFlow();
                    texteditor.getChildren().add(new Text(item.content));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }
            }
            case IMAGE -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    ImageView v = new ImageView();
                    v.setPreserveRatio(true);
                    v.setFitWidth(250);
                    v.setFitHeight(250);

                    if (item.content != null) {
                        v.setImage(FileAccessLayer.getInstance().getImageFromString(item.content).getValue());
                    }

                    Button selectFileButton = new Button("", new FontIcon("icm-link"));

                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.label = newText;
                    });

                    selectFileButton.onActionProperty().set(e -> {
                        FileChooser chooser = new FileChooser();
                        File selectedFile = chooser.showOpenDialog(null);
                        if (selectedFile != null) {
                            item.content = selectedFile.getAbsolutePath();
                            Map.Entry<String, Image> entry = FileAccessLayer.getInstance().getImageFromString(selectedFile.getAbsolutePath());
                            item.content = entry.getKey();
                            v.setImage(entry.getValue());
                        }
                    });


                    line.getChildren().add(label);
                    line.getChildren().add(v);
                    line.getChildren().add(selectFileButton);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    ImageView v = new ImageView();
                    v.setPreserveRatio(true);
                    v.setFitWidth(250);
                    v.setFitHeight(250);

                    if (item.content != null) {
                        v.setImage(FileAccessLayer.getInstance().getImageFromString(item.content).getValue());
                    }

                    line.getChildren().add(label);
                    line.getChildren().add(v);
                }
            }
            case HEADER -> {
                if (editable) {
                    VBox label = new VBox();
                    label.setPrefWidth(120);

                    TextField content = new TextField();
                    content.setAlignment(Pos.TOP_CENTER);
                    content.setText(item.content);
                    HBox.setHgrow(content, Priority.ALWAYS);

                    content.textProperty().addListener((editor, oldText, newText) -> {
                        item.content = newText;
                    });


                    line.getChildren().add(label);
                    line.getChildren().add(content);
                    line.getChildren().add(removeLine);
                } else {
                    VBox label = new VBox();
                    label.setPrefWidth(120);

                    TextFlow content = new TextFlow();
                    Text t = new Text(item.content);
                    t.setStyle("-fx-font-weight: bold;");
                    content.setTextAlignment(TextAlignment.CENTER);
                    content.getChildren().add(t);
                    HBox.setHgrow(content, Priority.ALWAYS);
                    line.getChildren().add(label);
                    line.getChildren().add(content);
                }
            }
            case RESOURCE -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextField value = new TextField(String.valueOf(item.value));
                    TextField maxValue = new TextField(String.valueOf(item.max));

                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.label = newText;
                    });

                    value.textProperty().addListener((editor, oldText, newText) -> {
                        item.value = Integer.valueOf(newText);
                    });
                    maxValue.textProperty().addListener((editor, oldText, newText) -> {
                        item.max = Integer.valueOf(newText);
                    });

                    HBox.setHgrow(value, Priority.ALWAYS);
                    HBox.setHgrow(maxValue, Priority.ALWAYS);

                    line.getChildren().add(label);
                    line.getChildren().add(value);
                    line.getChildren().add(new Label(" / "));
                    line.getChildren().add(maxValue);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.label);
                    TextField value = new TextField(String.valueOf(item.value));
                    Label maxValue = new Label(String.valueOf(item.max));

                    value.textProperty().addListener((editor, oldText, newText) -> {
                        item.value = Integer.valueOf(newText);
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(value);
                    line.getChildren().add(new Label(" / "));
                    line.getChildren().add(maxValue);
                }
            }
        }
        return line;
    }

    @Override
    public Node getPreviewVersionOf(ActorNoteDefinition t) {

        ScrollPane p = new ScrollPane();
        VBox lines = new VBox();
        if (t != null) {
            System.out.println("Rendering " + notesStructure.items.size() + " elements");


            t.items.forEach(item -> {
                HBox line = renderItem(item, false);
                lines.getChildren().add(line);
            });
        }
        return lines;
    }

    private Node getEditableVersion() {

        ObservableList<ActorNoteItem.ActorNoteItemType> types = FXCollections.observableArrayList();
        types.add(ActorNoteItem.ActorNoteItemType.HEADER);
        types.add(ActorNoteItem.ActorNoteItemType.STRING);
        types.add(ActorNoteItem.ActorNoteItemType.TEXT);
        types.add(ActorNoteItem.ActorNoteItemType.IMAGE);
        types.add(ActorNoteItem.ActorNoteItemType.RESOURCE);
        VBox lines = new VBox();

        if (notesStructure != null) {

            notesStructure.items.forEach(item -> {
                HBox line = renderItem(item);
                FontIcon fi = new FontIcon("icm-page-break");
                fi.setIconSize(20);

                Label dragButton = new Label("", fi);
                dragButton.setPrefWidth(24);
                dragButton.setPrefHeight(24);


                dragButton.onDragOverProperty().set(e -> {
                    fi.setIconColor(Color.BLUE);
                    dragposition = lines.getChildren().indexOf(line);
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });

                dragButton.onDragExitedProperty().set(e -> {
                    fi.setIconColor(Color.BLACK);
                });

                dragButton.onDragEnteredProperty().set(e -> {
                    fi.setIconColor(Color.BLUE);
                    dragposition = lines.getChildren().indexOf(line);
                    e.acceptTransferModes(TransferMode.MOVE);
                    e.consume();
                });

                dragButton.onDragDroppedProperty().set(e -> {
                    if (dragelement != null) {
                        notesStructure.items.remove(dragelement);
                        notesStructure.items.add(dragposition, dragelement);
                    }
                    editor.setContent(getEditableVersion());
                    e.setDropCompleted(true);
                    e.consume();
                });


                dragButton.onDragDetectedProperty().set(e -> {
                    dragelement = item;
                    dragposition = lines.getChildren().indexOf(line);
                    Dragboard db = dragButton.startDragAndDrop(TransferMode.ANY);
                    ClipboardContent c = new ClipboardContent();
                    c.putString("accepted");
                    db.setContent(c);

                    e.consume();
                });

                line.getChildren().add(0, dragButton);
                lines.getChildren().add(line);
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
                editor.setContent(getEditableVersion());

        });
        VBox v = new VBox();
        v.fillWidthProperty().set(true);
        newLine.getChildren().add(newType);
        newLine.getChildren().add(v);
        lines.getChildren().add(newLine);
        return lines;
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
                editor.setContent(getEditableVersion());
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
