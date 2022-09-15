package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.NoteType;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;
import javafx.util.Callback;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.event.MouseOverTextEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import org.fxmisc.richtext.StyleClassedTextArea;


import java.io.*;

public class TextNoteEditor implements EditorPlugin {

    private boolean contentHasChanged = false;

    private StyleClassedTextArea editor;
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

        boldButton.onActionProperty().set(e -> {
            IndexRange range = editor.getSelection();
            editor.setStyleClass(range.getStart(), range.getEnd(), "bold");
            editor.requestFocus();
        });

        italicButton.onActionProperty().set(e -> {
            IndexRange range = editor.getSelection();
            editor.setStyleClass(range.getStart(), range.getEnd(), "italic");
            editor.requestFocus();
        });

        underlinedButton.onActionProperty().set(e -> {
            IndexRange range = editor.getSelection();
            editor.setStyleClass(range.getStart(), range.getEnd(), "underlined");
            editor.requestFocus();
        });

        strikethroughButton.onActionProperty().set(e -> {
            IndexRange range = editor.getSelection();
            editor.setStyleClass(range.getStart(), range.getEnd(), "strikethrough");
            editor.requestFocus();
        });

        highlightButton.onActionProperty().set(e -> {
            IndexRange range = editor.getSelection();
            editor.setStyleClass(range.getStart(), range.getEnd(), "highlighted");
            editor.requestFocus();
        });

        t.setVisible(true);
    }

    public void onEditorKeyTyped(KeyEvent key) {
        contentHasChanged = true;
    }


    @Override
    public Node defineEditor() {

        editor = new StyleClassedTextArea();

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
                    editor.clear();
                    editor.appendText(content.toString());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        VirtualizedScrollPane<StyleClassedTextArea> vsPane = new VirtualizedScrollPane<>(editor);

        return vsPane;
    }

    @Override
    public Callback<Note, Boolean> defineSaveCallback() {
        return new Callback<Note, Boolean>() {
            @Override
            public Boolean call(Note note) {
                if (contentHasChanged) {

                    // TODO: save with style
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
                    editor.clear();
                    // TODO: load with style
                    editor.appendText(note.getContent());
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