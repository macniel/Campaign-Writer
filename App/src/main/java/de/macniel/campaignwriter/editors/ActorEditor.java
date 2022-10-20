package de.macniel.campaignwriter.editors;

import com.google.gson.stream.JsonWriter;
import de.macniel.campaignwriter.SDK.*;
import de.macniel.campaignwriter.SDK.types.Actor;
import de.macniel.campaignwriter.SDK.types.ActorNote;
import de.macniel.campaignwriter.SDK.types.ActorNoteItem;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.UUID;

public class ActorEditor extends EditorPlugin<ActorNote> implements ViewerPlugin<ActorNote> {

    private final ResourceBundle i18n;
    ActorNote actualNote;
    int dragposition;
    ActorNoteItem dragelement;
    private ScrollPane editor;
    private ComboBox setTemplateProp;
    private HashMap<String, Actor> actorTemplates;
    private Callback<UUID, Boolean> requester;

    public ActorEditor() {
        super();
        this.i18n = ResourceBundle.getBundle("i18n.buildingview");
    }

    @Override
    public String defineHandler() {
        return "building/actor";
    }

    @Override
    public Note createNewNote() {
        return new ActorNote();
    }

    @Override
    public void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback) {
        this.requester = stringBooleanCallback;
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();

        ToggleGroup viewMode = new ToggleGroup();

        ToggleButton previewMode = new ToggleButton(i18n.getString("PreviewMode"));
        ToggleButton editMode = new ToggleButton(i18n.getString("EditMode"));
        Button saveAsTemplateButton = new Button(i18n.getString("SaveTemplateAs"), new FontIcon("icm-floppy-disk"));
        setTemplateProp = new ComboBox<>();

        previewMode.setToggleGroup(viewMode);
        editMode.setToggleGroup(viewMode);

        viewMode.selectedToggleProperty().addListener((observableValue, toggle, newValue) -> {
            if (previewMode.equals(newValue)) {
                editor.setContent(getPreviewVersionOf(actualNote));
                setTemplateProp.setDisable(true);
                saveAsTemplateButton.setDisable(true);
            } else {
                editor.setContent(getEditableVersion());
                setTemplateProp.setDisable(false);
                saveAsTemplateButton.setDisable(false);
            }
        });

        viewMode.selectToggle(editMode);

        t.getItems().add(previewMode);
        t.getItems().add(editMode);

        actorTemplates = new FileAccessLayerFactory().get().getTemplates();
        setTemplateProp.getItems().clear();
        setTemplateProp.getItems().add("");
        actorTemplates.keySet().forEach(actorTemplateName -> setTemplateProp.getItems().add(actorTemplateName));
        setTemplateProp.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (actorTemplates != null && actorTemplates.get(newValue) != null) {
                updateActorSheetToTemplate(actorTemplates.get(newValue));
            }
        });

        t.getItems().addAll(new Label(i18n.getString("TemplateLabel")), setTemplateProp);

        saveAsTemplateButton.onActionProperty().set(event -> {
            saveTemplate(w);
        });

        t.getItems().add(saveAsTemplateButton);

        t.setVisible(true);
    }

    void saveTemplate(Window owner) {
        ArrayList<ActorNoteItem> sanitized = new ArrayList<>();
        actualNote.getContentAsObject().getItems().forEach(item -> {
            ActorNoteItem tmp = new ActorNoteItem();
            tmp.setLabel(item.getLabel());
            tmp.setType(item.getType());
            if (item.getType() == ActorNoteItem.ActorNoteItemType.HEADER) {
                tmp.setContent(item.getContent());
            }
            sanitized.add(tmp);
        });
        Actor def = new Actor();
        def.setItems(sanitized);

        FileChooser saveDialog = new FileChooser();
        saveDialog.setTitle(i18n.getString("SaveTemplateDialogTitle"));
        saveDialog.setInitialDirectory(Paths.get(System.getProperty("user.home"), ".campaignwriter", "templates").toFile());
        File f = saveDialog.showSaveDialog(owner);
        try (JsonWriter writer = new JsonWriter(new FileWriter(f))) {

            new FileAccessLayerFactory().get().getParser().toJson(def, Actor.class, writer);
            writer.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void updateActorSheetToTemplate(Actor template) {

        ArrayList<ActorNoteItem> merged = new ArrayList<>();

        template.getItems().forEach(templateItem -> {
            actualNote.getContentAsObject().getItems().stream().filter(item ->
                    templateItem.getLabel().equals(item.getLabel())
            ).findFirst().ifPresentOrElse(previousValue -> {
                ActorNoteItem tmp = new ActorNoteItem();
                tmp.setContent(previousValue.getContent());
                tmp.setMax(previousValue.getMax());
                tmp.setValue(previousValue.getValue());
                tmp.setType(previousValue.getType());
                tmp.setLabel(templateItem.getLabel());
                merged.add(tmp);
            }, () -> {
                ActorNoteItem tmp = new ActorNoteItem();
                tmp.setContent(templateItem.getContent());
                tmp.setMax(templateItem.getMax());
                tmp.setValue(templateItem.getValue());
                tmp.setType(templateItem.getType());
                tmp.setLabel(templateItem.getLabel());
                merged.add(tmp);
            });
        });
        actualNote.getContentAsObject().setItems(merged);

        editor.setContent(getEditableVersion());
    }

    @Override
    public Node defineEditor() {

        editor = new ScrollPane();

        editor.setContent(getEditableVersion());

        return editor;
    }

    private HBox renderItem(ActorNoteItem item) {
        return renderItem(item, true);
    }

    private HBox renderItem(ActorNoteItem item, boolean editable) {
        HBox line = new HBox();
        Button removeLine = new Button("", new FontIcon("icm-bin"));
        removeLine.onActionProperty().set(e -> {
            actualNote.getContentAsObject().getItems().remove(item);
            editor.setContent(getEditableVersion());
        });


        switch (item.getType()) {
            case TEXT -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextArea texteditor = new TextArea(item.getContent());
                    texteditor.setWrapText(true);
                    texteditor.setPrefRowCount(3);

                    HBox.setHgrow(texteditor, Priority.ALWAYS);

                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.setLabel(newText);
                    });

                    texteditor.textProperty().addListener((editor, oldText, newText) -> {

                        item.setContent(newText);
                    });
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextFlow texteditor = new TextFlow();

                    texteditor.getChildren().add(new Text(item.getContent()));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }

            }
            case STRING -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextField texteditor = new TextField(item.getContent());
                    HBox.setHgrow(texteditor, Priority.ALWAYS);
                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.setLabel(newText);
                    });

                    texteditor.textProperty().addListener((editor, oldText, newText) -> {

                        item.setContent(newText);
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextFlow texteditor = new TextFlow();
                    texteditor.getChildren().add(new Text(item.getContent()));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }
            }
            case IMAGE -> {
                if (editable) {
                    TextField label = new TextField();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    ImageView v = new ImageView();
                    v.setPreserveRatio(true);
                    v.setFitWidth(250);
                    v.setFitHeight(250);

                    if (item.getContent() != null) {
                        new FileAccessLayerFactory().get().getImageFromString(item.getContent()).ifPresent(imgEntry -> {
                            item.setContent(imgEntry.getKey());
                            v.setImage(imgEntry.getValue());
                        });
                    }

                    Button selectFileButton = new Button("", new FontIcon("icm-link"));

                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.setLabel(newText);
                    });

                    selectFileButton.onActionProperty().set(e -> {
                        FileChooser chooser = new FileChooser();
                        File selectedFile = chooser.showOpenDialog(null);
                        if (selectedFile != null) {
                            item.setContent(selectedFile.getAbsolutePath());
                            new FileAccessLayerFactory().get().getImageFromString(selectedFile.getAbsolutePath()).ifPresent(entry -> {
                                item.setContent(entry.getKey());
                                v.setImage(entry.getValue());
                            });
                        }
                    });


                    line.getChildren().add(label);
                    line.getChildren().add(v);
                    line.getChildren().add(selectFileButton);
                    line.getChildren().add(removeLine);
                } else {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    ImageView v = new ImageView();
                    v.setPreserveRatio(true);
                    v.setFitWidth(250);
                    v.setFitHeight(250);

                    if (item.getContent() != null) {
                        new FileAccessLayerFactory().get().getImageFromString(item.getContent()).ifPresent(value -> {
                            item.setContent(value.getKey());
                            v.setImage(value.getValue());
                        });
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
                    content.setText(item.getContent());
                    HBox.setHgrow(content, Priority.ALWAYS);

                    content.textProperty().addListener((editor, oldText, newText) -> {
                        item.setContent(newText);
                    });


                    line.getChildren().add(label);
                    line.getChildren().add(content);
                    line.getChildren().add(removeLine);
                } else {
                    VBox label = new VBox();
                    label.setPrefWidth(120);

                    TextFlow content = new TextFlow();
                    Text t = new Text(item.getContent());
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
                    label.setText(item.getLabel());
                    TextField value = new TextField(String.valueOf(item.getValue()));
                    TextField maxValue = new TextField(String.valueOf(item.getMax()));

                    label.textProperty().addListener((editor, oldText, newText) -> {
                        item.setLabel(newText);
                    });

                    value.textProperty().addListener((editor, oldText, newText) -> {
                        item.setValue(Integer.valueOf(newText));
                    });
                    maxValue.textProperty().addListener((editor, oldText, newText) -> {
                        item.setMax(Integer.valueOf(newText));
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
                    label.setText(item.getLabel());
                    TextField value = new TextField(String.valueOf(item.getValue()));
                    Label maxValue = new Label(String.valueOf(item.getMax()));

                    value.textProperty().addListener((editor, oldText, newText) -> {
                        item.setValue(Integer.valueOf(newText));
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
    public Node getPreviewVersionOf(ActorNote t) {

        ScrollPane p = new ScrollPane();
        VBox lines = new VBox();
        lines.setFillWidth(true);
        if (t != null) {
            t.getContentAsObject().getItems().forEach(item -> {
                HBox line = renderItem(item, false);
                HBox.setHgrow(line, Priority.ALWAYS);
                lines.getChildren().add(line);
            });
        }
        p.setContent(lines);
        return p;
    }

    @Override
    public Node getStandaloneVersion(ActorNote t, Stage wnd) {
        VBox n = new VBox();

        n.setFillWidth(true);
        t.getContentAsObject().getItems().forEach(item -> {
            HBox line = new HBox();
            HBox.setHgrow(line, Priority.ALWAYS);
            switch (item.getType()) {
                case TEXT -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextFlow texteditor = new TextFlow();

                    texteditor.getChildren().add(new Text(item.getContent()));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }
                case STRING -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextFlow texteditor = new TextFlow();
                    texteditor.getChildren().add(new Text(item.getContent()));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }
                case IMAGE -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    ImageView v = new ImageView();
                    v.setPreserveRatio(true);
                    v.setFitWidth(250);
                    v.setFitHeight(250);

                    if (item.getClass() != null) {
                        new FileAccessLayerFactory().get().getImageFromString(item.getContent()).ifPresent(value -> {
                            item.setContent(value.getKey());
                            v.setImage(value.getValue());
                        });
                    }

                    line.getChildren().add(label);
                    line.getChildren().add(v);
                }
                case HEADER -> {
                    VBox label = new VBox();
                    label.setPrefWidth(120);

                    TextFlow content = new TextFlow();
                    Text text = new Text(item.getContent());
                    text.setStyle("-fx-font-weight: bold;");
                    content.setTextAlignment(TextAlignment.CENTER);
                    content.getChildren().add(text);
                    HBox.setHgrow(content, Priority.ALWAYS);
                    line.getChildren().add(label);
                    line.getChildren().add(content);
                }
                case RESOURCE -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextField value = new TextField(String.valueOf(item.getValue()));
                    Label maxValue = new Label(String.valueOf(item.getMax()));

                    value.textProperty().addListener((editor, oldText, newText) -> {
                        item.setValue(Integer.valueOf(newText));
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(value);
                    line.getChildren().add(new Label(" / "));
                    line.getChildren().add(maxValue);
                }
            }
            n.getChildren().add(line);
        });
        return n;
    }

    private Node getEditableVersion() {

        ObservableList<ActorNoteItem.ActorNoteItemType> types = FXCollections.observableArrayList();
        types.add(ActorNoteItem.ActorNoteItemType.HEADER);
        types.add(ActorNoteItem.ActorNoteItemType.STRING);
        types.add(ActorNoteItem.ActorNoteItemType.TEXT);
        types.add(ActorNoteItem.ActorNoteItemType.IMAGE);
        types.add(ActorNoteItem.ActorNoteItemType.RESOURCE);
        VBox lines = new VBox();

        if (actualNote != null) {

            actualNote.getContentAsObject().getItems().forEach(item -> {
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
                        actualNote.getContentAsObject().getItems().remove(dragelement);
                        actualNote.getContentAsObject().getItems().add(dragposition, dragelement);
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
        newType.setPromptText(i18n.getString("AddPropLine"));
        newType.setPrefWidth(120);
        newType.onActionProperty().set(e -> {
            ActorNoteItem added = new ActorNoteItem();
            added.setType(newType.getValue());
            actualNote.getContentAsObject().getItems().add(added);
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
    public Callback<Boolean, ActorNote> defineSaveCallback() {
        return p -> actualNote;
    }

    @Override
    public Callback<ActorNote, Boolean> defineLoadCallback() {
        return note -> {
            actualNote = note;
            editor.setContent(getEditableVersion());
            return true;
        };
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerViewer(this);
        registry.registerEditor(this);
        registry.registerType("actor", ActorNote.class);

    }
}
