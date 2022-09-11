package de.macniel.campaignwriter;

import javafx.scene.Node;
import javafx.scene.control.ListCell;

public final class NotesRenderer extends ListCell<Note> {

    private final NotesController notesController = new NotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(Note note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }

    }
}
