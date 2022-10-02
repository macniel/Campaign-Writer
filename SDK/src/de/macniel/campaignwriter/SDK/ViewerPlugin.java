package de.macniel.campaignwriter.SDK;

import javafx.scene.Node;
import javafx.stage.Stage;

public interface ViewerPlugin<NOTE_DEFINITION> extends Registrable {

    Node getPreviewVersionOf(NOTE_DEFINITION t);

    Node getStandaloneVersion(NOTE_DEFINITION t, Stage wnd);

    String defineHandler();
}
