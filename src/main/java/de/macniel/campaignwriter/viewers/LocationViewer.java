package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class LocationViewer implements ViewerPlugin {
    @Override
    public NoteType defineNoteType() {
        return NoteType.LOCATION_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        System.out.println("Rendering Location " + note.label + " as standalone");
        return new HBox(new Label("Willkommen in " + note.label));
    }

}
