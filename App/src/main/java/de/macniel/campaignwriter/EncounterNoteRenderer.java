package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.types.EncounterNote;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public final class EncounterNoteRenderer extends ListCell<EncounterNote> {

    private final NotesController notesController = new NotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(EncounterNote note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }
    }

}
