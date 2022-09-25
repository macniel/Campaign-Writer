package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.PictureNoteDefinition;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class PictureViewer implements ViewerPlugin<Note> {
    @Override
    public NoteType defineNoteType() {
        return NoteType.PICTURE_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        return null;
    }
}
