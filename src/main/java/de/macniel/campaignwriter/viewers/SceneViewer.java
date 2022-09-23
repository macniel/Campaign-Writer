package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class SceneViewer implements ViewerPlugin {
    @Override
    public NoteType defineNoteType() {
        return NoteType.SCENE_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new VBox();
    }

    @Override
    public Node renderNoteStandalone(Note note) {
        return null;
    }
}
