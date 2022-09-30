package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.types.Text;
import de.macniel.campaignwriter.types.TextNote;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Window;
import javafx.util.Callback;

import org.kordamp.ikonli.javafx.FontIcon;


import java.io.*;

public class TextNoteEditor extends EditorPlugin<TextNote> {

    private boolean contentHasChanged = false;

    private TextArea editor;
    private Callback<String, Note> onNoteRequest;
    private TextNote notesStructure;

    @Override
    public String defineHandler() {
        return "building/text";
    }

    @Override
    public void prepareToolbar(Node n, Window w) {
        ToolBar t = (ToolBar) n;

        t.getItems().clear();

        Button boldButton = new Button("", new FontIcon("icm-bold"));
        Button italicButton = new Button("", new FontIcon("icm-italic"));
        Button underlinedButton = new Button("", new FontIcon("icm-underline"));
        Button strikethroughButton = new Button("", new FontIcon("icm-strikethrough"));
        Button highlightButton = new Button("", new FontIcon("icm-price-tag"));

        t.getItems().add(boldButton);
        t.getItems().add(italicButton);
        t.getItems().add(underlinedButton);
        t.getItems().add(strikethroughButton);
        t.getItems().add(highlightButton);

        boldButton.onActionProperty().set(e -> {
            editor.requestFocus();
        });

        italicButton.onActionProperty().set(e -> {
            editor.requestFocus();
        });

        underlinedButton.onActionProperty().set(e -> {
            editor.requestFocus();
        });

        strikethroughButton.onActionProperty().set(e -> {
            editor.requestFocus();
        });

        highlightButton.onActionProperty().set(e -> {
            editor.requestFocus();
        });

        t.setVisible(true);
    }

    public void onEditorKeyTyped(KeyEvent key) {
        contentHasChanged = true;
    }


    @Override
    public Node defineEditor() {

        editor = new TextArea();

        editor.onKeyTypedProperty().set(this::onEditorKeyTyped);

        editor.setOnDragDetected(e -> {
            editor.startDragAndDrop(TransferMode.ANY);
            e.consume();
        });

        editor.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.LINK);
            }
            e.consume();
        });

        editor.setOnDragDropped(e -> {
            if (e.getDragboard().hasFiles()) {
                File f = e.getDragboard().getFiles().get(0);
                try (BufferedReader fis = new BufferedReader(new FileReader(f))) {
                    StringBuffer content = new StringBuffer();
                    String line;
                    while ((line = fis.readLine()) != null) {
                        content.append(line +"\n");
                    }
                    editor.clear();
                    editor.appendText(content.toString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        return editor;
    }

    @Override
    public Callback<Boolean, TextNote> defineSaveCallback() {
        return note -> notesStructure;
    }

    @Override
    public Callback<TextNote, Boolean> defineLoadCallback() {
        return note -> {
            notesStructure = note;
            updateView();
            return true;
        };
    }

    void updateView() {
        editor.setText(notesStructure.content.getContent());
    }

    @Override
    public Node getPreviewVersionOf(TextNote t) {
        return null;
    }

    @Override
    public Node getStandaloneVersion(TextNote t) {
        return null;
    }

    @Override
    public Note createNewNote() {
        return new TextNote();
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerEditor(this);
    }


    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {

    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {

    }
}