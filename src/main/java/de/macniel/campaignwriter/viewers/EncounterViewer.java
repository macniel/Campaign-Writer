package de.macniel.campaignwriter.viewers;

import java.util.UUID;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import de.macniel.campaignwriter.editors.EncounterNote;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

public class EncounterViewer implements ViewerPlugin<EncounterNote> {
    @Override
    public NoteType defineNoteType() {
        return NoteType.ENCOUNTER_NOTE;
    }

    @Override
    public Node renderNote(EncounterNote note, ObservableDoubleValue parentWidth, Callback<UUID, Note> requester) {
        return new HBox();
    }

    @Override
    public Node renderNoteStandalone(EncounterNote note) {
        return null;
    }
}
