package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.EditorPlugin;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import de.macniel.campaignwriter.SDK.ViewerPlugin;
import javafx.scene.input.KeyCodeCombination;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

public class Registry implements RegistryInterface {

    private ArrayList<EditorPlugin> editors = new ArrayList<>();
    private ArrayList<ViewerPlugin> viewers = new ArrayList<>();
    private ArrayList<Note> noteTypes = new ArrayList<>();

    private HashMap<KeyCodeCombination, Consumer<Note>> shortcuts = new HashMap<>();

    private static Registry instance = new Registry();

    public static Registry getInstance() {
        return Registry.instance;
    }

    @Override
    public void registerShortcut(KeyCodeCombination combination, Consumer<Note> callback) {
        shortcuts.put(combination, callback);
    }

    @Override
    public void registerType(Note type) {
        noteTypes.add(type);
    }

    @Override
    public void registerEditor(EditorPlugin editor) {
        editors.add(editor);
    }

    @Override
    public void registerViewer(ViewerPlugin viewer) {
        viewers.add(viewer);
    }

    public ArrayList<EditorPlugin> getEditorsByPrefix (String prefix) {
        return new ArrayList<>(editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().startsWith(prefix)).toList());
    }

    public Optional<EditorPlugin> getEditorByFullName (String fullName) {
        return editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().equals(fullName)).findFirst();
    }

    public ArrayList<ViewerPlugin> getAllViewers() {
        return viewers;
    }
}
