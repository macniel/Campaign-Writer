package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextViewer implements ViewerPlugin {


    @Override
    public NoteType defineNoteType() {
        return NoteType.TEXT_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {

        TextFlow f = new TextFlow() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                double maxChildWidth = 0;

                for (Node child : getManagedChildren()) {
                    if (child instanceof Text textNode) {
                       if (textNode.getText().startsWith("#")) {
                            
                        }
                        
                    }
                    double childWidth = child.getLayoutBounds().getWidth();
                    maxChildWidth = Math.max(maxChildWidth, childWidth);
                }
                double insetWidth = getInsets().getLeft() + getInsets().getRight();
                double adjustedWidth = maxChildWidth + insetWidth;

                setMaxWidth(adjustedWidth);
            }
        };
        parentWidth.addListener((observableValue, number, newWidth) -> {
            f.setMaxWidth(newWidth.doubleValue());
        });
        f.setMaxWidth(parentWidth.get());

        String[] lines = note.getContent().split("\n");
        for (String line : lines) {


            /**
             * group0: optional preceeding text
             * group1: link text
             * group2: optional text representing the link
             * group3: optional suceeding text
             */
            Pattern urlPattern = Pattern.compile("(?<p>.*)?\\[(?<l>.*)\\](\\((?<t>.*)\\))?(?<s>.*)?");

            Matcher matches = urlPattern.matcher(line);

            if (matches.find()) {
                System.out.println("Groups found: "+ matches.groupCount());
                if (matches.group("p") != null) {
                    // TODO: rethink, this could contain other md elements
                    f.getChildren().add(new Text(matches.group("p")));
                    System.out.println("Rendering preceeding text with content: " + matches.group("p"));
                } 
                if (matches.group("l") != null) {
                    Text link = new Text();
                    System.out.print("Rendering link targetting: " + matches.group("l"));
                    if (matches.group("t") != null) {
                        link.setText(matches.group("t"));
                        System.out.println("With label " + matches.group("t"));
                    
                    } else {
                        link.setText(matches.group("l"));
                        System.out.println("With no label ");
                    }
                    FileAccessLayer.getInstance().findByLabel(matches.group("l")).ifPresent(linkTargetObject -> {
                        System.out.println("acquired link target uuid " + linkTargetObject.reference + " with label " + linkTargetObject.label);
                            
                        link.onMouseClickedProperty().set(event -> {
                            requester.call(linkTargetObject.reference);
                        });
                    });
                    System.out.println(link.getText());
                    f.getChildren().add(link);
                    link.setFill(Color.BLUE);
                    link.setCursor(Cursor.DEFAULT);
                }
                if (matches.group("s") != null) {
                    // TODO: rethink, this could contain ot her md elements
                    f.getChildren().add(new Text(matches.group("s")));
                }
            } else { // line doesnt contain links, carry on
                for (String word : line.split(" ")) {
                    f.getChildren().add(new Text(word));
                    f.getChildren().add(new Text(" "));
                }
            }

            f.getChildren().add(new Text("\n"));
        }

        return new StackPane(f);
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        return null;
    }
}
