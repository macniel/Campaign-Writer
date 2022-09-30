package de.macniel.campaignwriter.SDK;

import javafx.scene.input.KeyCodeCombination;

import java.util.function.Consumer;

public interface RegistryInterface {

    void registerShortcut(KeyCodeCombination combination, Consumer<Note> callback);

    void registerType(Note type);

    void registerEditor(EditorPlugin editor);

    void registerViewer(ViewerPlugin viewer);

}
