package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.Note;
import javafx.scene.Node;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.UUID;

public interface ViewInterface<T> {

    public String getPathToFxmlDefinition();

    public String getMenuItemLabel();

    public abstract void requestLoad(ArrayList<T> items);

    public abstract void requestSave();

    abstract void requestNote(Callback<UUID, Note> cb);

}
