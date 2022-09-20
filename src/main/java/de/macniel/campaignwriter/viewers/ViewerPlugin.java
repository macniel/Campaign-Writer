package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;

public interface ViewerPlugin {

    NoteType defineNoteType();

    Node renderNote(Note note);

}
