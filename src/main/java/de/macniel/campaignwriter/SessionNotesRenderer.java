package de.macniel.campaignwriter;

import de.macniel.campaignwriter.editors.SessionNote;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public final class SessionNotesRenderer extends ListCell<SessionNote> {

    private final SessionNotesController notesController = new SessionNotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(SessionNote note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }
    }

}
