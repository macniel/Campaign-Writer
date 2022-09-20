package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.Note;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.UUID;

public class SessionView implements ViewInterface<Note> {
    private Callback<UUID, Note> requester;

    @Override
    public String getPathToFxmlDefinition() {
        return "session-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return "Sitzung";
    }

    @Override
    public void requestLoad(ArrayList<Note> items) {

    }

    @Override
    public void requestSave() {

    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }
}
