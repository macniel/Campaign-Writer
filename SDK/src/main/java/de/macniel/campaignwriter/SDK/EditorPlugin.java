package de.macniel.campaignwriter.SDK;

import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;

public abstract class EditorPlugin<NOTE_DEFINITION> implements Registrable {

    /**
     * Editor Handler are defined as: [module]/[notetype]
     * @return
     */
    public abstract String defineHandler();

    /**
     * On top of the editor resides a ToolBar which is configured inside this method
     * @param t
     * @param w
     */
    public abstract void prepareToolbar(Node t, Window w);

    /**
     * Create an GUI to edit the handled Note
     * @return
     */
    public abstract Node defineEditor();

    /**
     * Save the Note inside this editor to the campaign file
     * @return
     */
    public abstract Callback<Boolean, NOTE_DEFINITION> defineSaveCallback();

    /**
     * Load the given Note inside this editor
     * @return
     */
    public abstract Callback<NOTE_DEFINITION, Boolean> defineLoadCallback();

    /**
     * Will be called by a Module to create a new note
     * @return
     */
    public abstract Note createNewNote();

    /**
     * Callback will be used to trigger to load a note either in the same editor or a different one
     * @param stringBooleanCallback
     */
    public abstract void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback);
}
