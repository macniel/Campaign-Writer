package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import de.macniel.campaignwriter.SDK.types.CombatantNote;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Callback;

public class CombatantViewer implements ViewerPlugin<CombatantNote> {
    private Callback<CombatantNote, Boolean> changeCallback;

    @Override
    public void register(RegistryInterface registry) {
        registry.registerViewer(this);
    }

    @Override
    public String defineHandler() {
        return "encounter/combatant";
    }

    @Override
    public Node getPreviewVersionOf(CombatantNote t) {
        VBox box = new VBox();

        HBox meta = new HBox();
        Label initiativePropLabel = new Label("Initative");
        initiativePropLabel.setPrefWidth(120);
        TextField initiativeProp = new TextField(t.getContentAsObject().getInitiative());

        initiativeProp.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                t.getContentAsObject().setInitiative(newValue);
            }
        });


        meta.getChildren().addAll(initiativePropLabel, initiativeProp);
        box.getChildren().add(meta);

        t.getContentAsObject().getItems().forEach(item -> {
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
                    Text txt = new Text(item.getContent());
                    texteditor.getChildren().add(txt);
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
                    TextFlow content = new TextFlow();
                    Text txt = new Text(item.getContent());
                    txt.setStyle("-fx-font-weight: bold;");
                    content.setTextAlignment(TextAlignment.CENTER);
                    content.getChildren().add(txt);
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
                                changeCallback.call(t);
                            }
                        }
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(value);
                    line.getChildren().add(new Label(" / "));
                    line.getChildren().add(maxValue);
                }
            }

            box.getChildren().add(line);
        });
        return box;
    }

    public void setChangeCallback(Callback<CombatantNote, Boolean> changeCallback) {
        this.changeCallback = changeCallback;
    }

    @Override
    public Node getStandaloneVersion(CombatantNote t, Stage wnd) {
        VBox box = new VBox();

        t.getContentAsObject().getItems().forEach(item -> {
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
                    Text txt = new Text(item.getContent());
                    texteditor.getChildren().add(txt);
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
                    TextFlow content = new TextFlow();
                    Text txt = new Text(item.getContent());
                    txt.setStyle("-fx-font-weight: bold;");
                    content.setTextAlignment(TextAlignment.CENTER);
                    content.getChildren().add(txt);
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
                                changeCallback.call(t);
                            }
                        }
                    });

                    line.getChildren().add(label);
                    line.getChildren().add(value);
                    line.getChildren().add(new Label(" / "));
                    line.getChildren().add(maxValue);
                }
            }

            box.getChildren().add(line);
        });
        return box;
    }
}
