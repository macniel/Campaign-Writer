package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.Window;
import javafx.util.Callback;

import org.kordamp.ikonli.javafx.FontIcon;

import java.io.*;

public class TextNoteEditor implements EditorPlugin {

    private boolean contentHasChanged = false;

    private TextArea editor;
    private Callback<String, Note> onNoteRequest;
    private Callback<String, Boolean> onNoteLoadRequest;

    @Override
    public NoteType defineHandler() {
        return NoteType.TEXT_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t, Window w) {
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

        highlightButton.onActionProperty().set(e -> {
            // example code to request loading a note referenced by uuid/string
            int size = Note.getAll().size();
            int randomed = (int) Math.floor(Math.random() * size);
            String uuid = Note.getAll().get(randomed).getReference().toString();
            onNoteLoadRequest.call(uuid);
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
                try {
                    StringBuffer content = new StringBuffer();
                    BufferedReader fis = new BufferedReader(new FileReader(f));
                    String line;
                    while ((line = fis.readLine()) != null) {
                        content.append(line +"\n");
                    }
                    editor.setText(content.toString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        return editor;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (contentHasChanged) {
                    note.setContent(editor.getText());
                }
                return true;
            }
        };
    }

    @Override
    public Callback<Note, Boolean> defineLoadCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (note.getType() == NoteType.TEXT_NOTE) {
                    editor.setText(note.getContent());
                    contentHasChanged = false;
                    editor.requestFocus();
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void setOnNoteRequest(Callback<String, Note> stringNoteCallback) {
        this.onNoteRequest = stringNoteCallback;
    }

    @Override
    public void setOnNoteLoadRequest(Callback<String, Boolean> stringBooleanCallback) {
        this.onNoteLoadRequest = stringBooleanCallback;
    }

}