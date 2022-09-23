package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.util.Callback;

public interface ViewerPlugin {

    NoteType defineNoteType();

    Node renderNote(Note note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester);

    Node renderNoteStandalone(Note note);

}
