package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.Note;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TreeCell;

public final class NotesTreeRenderer extends TreeCell<Note> {

    private final NotesController notesController = new NotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(Note note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else if (note == null) {
            setGraphic(new Label("Kampagne"));
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }
    }

}
