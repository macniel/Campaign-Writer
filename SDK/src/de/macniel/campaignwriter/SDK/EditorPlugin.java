package de.macniel.campaignwriter.SDK;

import javafx.scene.Node;
import javafx.stage.Window;
import javafx.util.Callback;

public abstract class EditorPlugin<NOTE_DEFINITION> implements Registrable {

    public abstract String defineHandler();

    public abstract void prepareToolbar(Node t, Window w);

    public abstract Node defineEditor();

    public abstract Callback<Boolean, ? extends Note> defineSaveCallback();

    public abstract Callback<? extends Note, Boolean> defineLoadCallback();

    public abstract Node getPreviewVersionOf(NOTE_DEFINITION t);

    public abstract Node getStandaloneVersion(NOTE_DEFINITION t);

    public abstract Note createNewNote();

    public abstract void setOnNoteRequest(Callback<String, Note> stringNoteCallback);

    public abstract void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback);
}
