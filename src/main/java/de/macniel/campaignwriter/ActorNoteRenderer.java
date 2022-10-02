package de.macniel.campaignwriter;

import de.macniel.campaignwriter.types.ActorNote;
import de.macniel.campaignwriter.types.LocationNote;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public final class ActorNoteRenderer extends ListCell<ActorNote> {

    private final NotesController notesController = new NotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(ActorNote note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }
    }

}
