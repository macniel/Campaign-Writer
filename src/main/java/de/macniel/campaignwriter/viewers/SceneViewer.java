package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class SceneViewer implements ViewerPlugin {
    @Override
    public NoteType defineNoteType() {
        return NoteType.SCENE_NOTE;
    }

    @Override
    public Node renderNote(Note note, ObservableDoubleValue parentWidth) {
        return new VBox();
    }
}
