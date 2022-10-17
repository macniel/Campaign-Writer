package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.types.ActorNote;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

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
            ((HBox) view).setPadding(Insets.EMPTY);
            setGraphic(view);
        }
    }

}
