package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;

public interface ViewerPlugin {

    NoteType defineNoteType();

    Node renderNote(Note note, ObservableDoubleValue parentWidth);

    Node renderNoteStandalone(Note note);

}
