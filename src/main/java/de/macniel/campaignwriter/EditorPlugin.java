package de.macniel.campaignwriter;

import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.util.Callback;

public interface EditorPlugin {

    NoteType defineHandler();

    void prepareToolbar(ToolBar t);

    Node defineEditor();

    Callback<Note, Boolean> defineSaveCallback();

    Callback<Note, Boolean> defineLoadCallback();

    void loadContent(Note note);

    void saveContent(Note note);
}
