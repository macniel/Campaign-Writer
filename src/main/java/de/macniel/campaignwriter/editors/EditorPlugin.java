package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.stage.Window;
import javafx.util.Callback;

public interface EditorPlugin {

    NoteType defineHandler();

    void prepareToolbar(ToolBar t, Window w);

    Node defineEditor();

    Callback<Note, Boolean> defineSaveCallback();

    Callback<Note, Boolean> defineLoadCallback();

}
