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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Map;

public class MapViewer implements ViewerPlugin {

    Gson gsonParser = new GsonBuilder().registerTypeAdapter(Color.class, new ColorAdapter()).create();

    @Override
    public NoteType defineNoteType() {
        return NoteType.MAP_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue width) {


        ScrollPane p = new ScrollPane();
        p.setPannable(true);
        System.out.println(note.type);

        MapNoteDefinition noteStructure = gsonParser.fromJson(note.content, MapNoteDefinition.class);
        if (noteStructure != null) {

            Map.Entry<String, Image> entry = FileAccessLayer.getInstance().getImageFromString(noteStructure.backgroundPath);
            ImageView view = new ImageView(entry.getValue());

            width.addListener( (observable, oldValue, newValue) -> {
                p.setMaxWidth(newValue.doubleValue());
            });

            p.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            p.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

            p.setContent(view);
            p.setPrefHeight(400);
        }

        return p;
    }
}
