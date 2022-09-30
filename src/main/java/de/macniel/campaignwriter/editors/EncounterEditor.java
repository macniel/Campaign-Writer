package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.types.*;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Window;
import javafx.util.Callback;

public class EncounterEditor extends EditorPlugin<CombatantNote> {

    @Override
    public String defineHandler() {
        return "encounter/combatant";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;
        t.getItems().clear();

        t.setVisible(true);
    }

    @Override
    public Node defineEditor() {
        return null;
    }

    @Override
    public Callback<Boolean, EncounterNote> defineSaveCallback() {
        return null;
    }

    @Override
    public Callback<EncounterNote, Boolean> defineLoadCallback() {
        return null;
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
    public Node getPreviewVersionOf(CombatantNote note) {
        VBox n = new VBox();

        note.content.getItems().forEach(item -> {
            HBox line = new HBox();
            switch(item.getType()) {
                case TEXT -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    label.setMinWidth(120);
                    TextFlow texteditor = new TextFlow();

                    HBox.setHgrow(texteditor, Priority.SOMETIMES);
                    texteditor.getChildren().add(new Text(item.getContent()));
                    line.getChildren().add(label);
                    line.getChildren().add(texteditor);
                }
                case STRING -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setMinWidth(120);
                    label.setText(item.getLabel());
                    TextFlow texteditor = new TextFlow();

                    HBox.setHgrow(texteditor, Priority.SOMETIMES);
                    Text t = new Text(item.getContent());
                    texteditor.getChildren().add(t);
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
                        FileAccessLayer.getInstance().getImageFromString(item.getContent()).ifPresent(value -> v.setImage(value.getValue()));
                    }

                    line.getChildren().add(label);
                    line.getChildren().add(v);
                }
                case HEADER -> {
                    TextFlow content = new TextFlow();
                    Text t = new Text(item.getContent());
                    t.setStyle("-fx-font-weight: bold;");
                    content.setTextAlignment(TextAlignment.CENTER);
                    content.getChildren().add(t);
                    HBox.setHgrow(content, Priority.ALWAYS);
                    line.getChildren().add(content);
                }
                case RESOURCE -> {
                    Label label = new Label();
                    label.setPrefWidth(120);
                    label.setText(item.getLabel());
                    TextField value = new TextField(String.valueOf(item.getValue()));
                    Label maxValue = new Label(String.valueOf(item.getMax()));

                    value.onKeyReleasedProperty().set( event -> {
                        if (event.getCode() == KeyCode.ENTER) {
                            if (value.getText().indexOf("-") >= 1) {
                                int opr1 = Integer.parseInt(value.getText().split("-")[0]);
                                int opr2 = Integer.parseInt(value.getText().split("-")[1]);
                                value.setText(String.valueOf((opr1 - opr2)));

                            } else if (value.getText().indexOf("+") >= 1) {
                                int opr1 = Integer.parseInt(value.getText().split("\\+")[0]);
                                int opr2 = Integer.parseInt(value.getText().split("\\+")[1]);
                                value.setText(String.valueOf((opr1 + opr2)));

                            }
                            item.setValue(Integer.valueOf(value.getText()));
                            if (changeCallback != null) {
                                changeCallback.call(item);
                            }
                        }
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

    @Override
    public Node getStandaloneVersion(CombatantNote t) {
        return null;
    }

    @Override
    public Note createNewNote() {
        return new EncounterNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
    }
}
