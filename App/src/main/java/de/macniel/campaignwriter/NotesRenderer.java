package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.Note;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;

public final class NotesRenderer extends ListCell<Note> {

    private boolean flatList;

    private final NotesController notesController;
    private final Node view;


    public NotesRenderer(boolean flatList) {
        this();
        this.flatList = flatList;
    }

    public NotesRenderer() {
        this.flatList = false;
        this.notesController = new NotesController(null);
        this.view = notesController.getView();
    }


    @Override
    protected void updateItem(Note note, boolean empty) {
        super.updateItem(note, empty);
        if (empty || note == null) {
            setGraphic(null);
        } else {
            notesController.setItem(note);

            if (flatList) {
                ((HBox) view).setPadding(Insets.EMPTY);
            }

            setGraphic(view);
        }
    }

}
