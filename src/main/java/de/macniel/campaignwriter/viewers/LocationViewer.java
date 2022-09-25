package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.LocationNoteDefinition;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class LocationViewer implements ViewerPlugin<LocationNoteDefinition> {
    @Override
    public NoteType defineNoteType() {
        return NoteType.LOCATION_NOTE;
    }

    @Override
    public Node renderNote(LocationNoteDefinition note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(LocationNoteDefinition note) {
        System.out.println("Rendering Location " + note.getCanonicalName() + " as standalone");
        return new HBox(new Label("Willkommen in " + note.getCanonicalName()));
    }

}
