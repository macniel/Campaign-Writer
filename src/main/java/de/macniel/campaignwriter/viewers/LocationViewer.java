package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class LocationViewer implements ViewerPlugin {
    @Override
    public NoteType defineNoteType() {
        return NoteType.LOCATION_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        return null;
    }
}
