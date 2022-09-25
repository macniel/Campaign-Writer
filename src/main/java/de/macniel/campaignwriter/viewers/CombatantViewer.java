package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.ActorNoteItem;
import de.macniel.campaignwriter.editors.Combatant;
import javafx.beans.value.ObservableDoubleValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CombatantViewer {

    public NoteType defineNoteType() {
        return NoteType.COMBATANT_NOTE;
    }

    public Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new VBox();
    }

    private Callback<ActorNoteItem, Boolean> changeCallback;

    public void setChangeCallback(Callback<ActorNoteItem, Boolean> changeCallback) {
        this.changeCallback = changeCallback;
    }


    public Node renderNoteStandalone(Combatant note, Stage g) {
        VBox n = new VBox();

        note.getItems().forEach(item -> {
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
                                    int opr1 = Integer.valueOf(value.getText().split("-")[0]);
                                    int opr2 = Integer.valueOf(value.getText().split("-")[1]);
                                    value.setText(String.valueOf((opr1 - opr2)));
                                    
                                } else if (value.getText().indexOf("+") >= 1) {
                                    int opr1 = Integer.valueOf(value.getText().split("+")[0]);
                                    int opr2 = Integer.valueOf(value.getText().split("+")[1]);
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
            
            g.widthProperty().addListener((observable, oldValue, newValue) -> {
                line.setPrefWidth(newValue.doubleValue());
            });
            n.getChildren().add(line);
        });
        return n;
    }
    
}
