package de.macniel.campaignwriter.viewers;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class TextViewer implements ViewerPlugin {


    @Override
    public NoteType defineNoteType() {
        return NoteType.TEXT_NOTE;
    }

    @Override
    public Node renderNote(Note note) {
        Label l = new Label(note.content);

        return l;
    }
}
