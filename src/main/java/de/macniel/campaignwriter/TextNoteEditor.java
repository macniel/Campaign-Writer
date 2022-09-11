package de.macniel.campaignwriter;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;

public class TextNoteEditor implements EditorPlugin {

    private boolean contentHasChanged = false;

    private TextArea editor;

    @Override
    public NoteType defineHandler() {
        return NoteType.TEXT_NOTE;
    }

    @Override
    public void prepareToolbar(ToolBar t) {
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
        t.setVisible(true);
    }

    public void onEditorKeyTyped(KeyEvent key) {
        contentHasChanged = true;
        System.out.println("content has changed");
    }

    @Override
    public Node defineEditor() {
        editor = new TextArea();
        editor.onKeyTypedProperty().set(this::onEditorKeyTyped);
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
                System.out.println("saving editor");
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
                    System.out.println("loading editor");
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    public void loadContent(Note note) {
        editor.setText(note.getContent());
    }

    @Override
    public void saveContent(Note note) {
        note.setContent(editor.getText());
    }

}