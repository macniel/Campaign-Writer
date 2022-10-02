package de.macniel.campaignwriter.SDK;

import javafx.scene.Node;
import javafx.stage.Stage;

public interface ViewerPlugin<NOTE_DEFINITION> extends Registrable {
    /**
     * Get a preview non editable version of the given Note
     * @param t
     * @return
     */
    Node getPreviewVersionOf(NOTE_DEFINITION t);

    /**
     * Get a preview of the given Note but inside a window as a popout
     * @param t
     * @param wnd
     * @return
     */
    Node getStandaloneVersion(NOTE_DEFINITION t, Stage wnd);

    String defineHandler();
}
