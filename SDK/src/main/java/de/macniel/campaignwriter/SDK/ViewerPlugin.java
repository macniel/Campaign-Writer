package de.macniel.campaignwriter.SDK;

import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.UUID;

public interface ViewerPlugin<NOTE_DEFINITION> extends Registrable {
    /**
     * Get a preview non editable version of the given Note
     *
     * @param t
     * @return
     */
    Node getPreviewVersionOf(NOTE_DEFINITION t);

    /**
     * Get a preview of the given Note but inside a window as a popout
     *
     * @param t
     * @param wnd
     * @return
     */
    Node getStandaloneVersion(NOTE_DEFINITION t, Stage wnd);

    /**
     * Callback will be used to trigger to load a note either in the same editor or a different one
     *
     * @param stringBooleanCallback
     */
    void setOnNoteLoadRequest(Callback<UUID, Boolean> stringBooleanCallback);

    String defineHandler();
}
