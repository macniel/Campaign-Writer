package de.macniel.campaignwriter.viewers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.adapters.ColorAdapter;
import de.macniel.campaignwriter.editors.MapNoteDefinition;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.util.UUID;

public class MapViewer implements ViewerPlugin<Note> {

    @Override
    public NoteType defineNoteType() {
        return NoteType.MAP_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue width, Callback<UUID, Note> requester) {

        VBox child = new VBox();

        ScrollPane p = new ScrollPane();
        p.setPannable(true);
        p.setMaxWidth(width.get());

        MapNoteDefinition noteStructure = FileAccessLayer.getInstance().getParser().fromJson(note.content, MapNoteDefinition.class);

        if (noteStructure != null) {

            FileAccessLayer.getInstance().getImageFromString(noteStructure.backgroundPath).ifPresent(entry -> {
                ImageView view = new ImageView(entry.getValue());

                view.setPreserveRatio(true);
                view.setFitHeight(view.getImage().getHeight() /2);
                
                width.addListener( (observable, oldValue, newValue) -> {
                    p.setMaxWidth(newValue.doubleValue());
                    view.setFitHeight(view.getImage().getHeight() /2);
                });
    
    
                view.onMouseClickedProperty().set(e -> {
                    if (e.getClickCount() == 2) {
                        e.consume();
                    }
                });
    
                p.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                p.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    
                p.setContent(view);
                p.setPrefHeight(400);
            });
            
        }

        child.getChildren().add(p);

        return child;
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        return null;
    }
}
