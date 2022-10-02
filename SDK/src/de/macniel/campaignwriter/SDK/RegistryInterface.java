package de.macniel.campaignwriter.SDK;

import javafx.scene.input.KeyCodeCombination;

import java.util.function.Consumer;

public interface RegistryInterface {

    /**
     * Register a shortcut to execute the callback
     * @param combination
     * @param callback
     */
    void registerShortcut(KeyCodeCombination combination, Consumer<Note> callback);

    /**
     * Register a new Note Type with the given name
     * @param typeName
     * @param type
     */
    void registerType(String typeName, Class<? extends Note> type);

    /**
     * Register a new Data Provider
     * @param dataProvider
     */
    void registerDataProvider(DataPlugin dataProvider);

    /**
     * Register a new Viewer which is used to display a Note with no Editor
     * @param viewer
     */
    void registerViewer(ViewerPlugin viewer);

    /**
     * Register a new Editor to edit a Note
     * @param editor
     */
    void registerEditor(EditorPlugin editor);

    /**
     * Register a new Module
     * @param module
     */
    void registerModule(ModulePlugin module);

}
