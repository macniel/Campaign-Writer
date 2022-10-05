package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.types.LocationNote;
import javafx.scene.Node;
import javafx.scene.control.ListCell;

public final class LocationNoteRenderer extends ListCell<LocationNote> {

    private final NotesController notesController = new NotesController(null);
    private final Node view = notesController.getView();

    @Override
    protected void updateItem(LocationNote note, boolean empty) {
        super.updateItem(note, empty);
        if (empty) {
            setGraphic(null);
        } else {
            notesController.setItem(note);
            setGraphic(view);
        }
    }

}
