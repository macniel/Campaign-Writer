package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.ActorNoteDefinition;
import de.macniel.campaignwriter.editors.Combatant;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;

public class ActorViewer implements ViewerPlugin<ActorNoteDefinition> {
    @Override
    public NoteType defineNoteType() {
        return NoteType.ACTOR_NOTE;
    }

    @Override
    public Node renderNote(ActorNoteDefinition note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(ActorNoteDefinition note) {
        VBox n = new VBox();
        note.getItems().forEach(item -> {
            HBox line = new HBox();
            switch(item.getType()) {
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
                        FileAccessLayer.getInstance().getImageFromString(item.getContent()).ifPresent(value -> v.setImage(value.getValue()));
                    }

                    line.getChildren().add(label);
                    line.getChildren().add(v);
                }
                case HEADER -> {
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
}
