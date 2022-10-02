package de.macniel.campaignwriter.SDK;

import javafx.scene.input.KeyCodeCombination;

import java.util.function.Consumer;

public interface RegistryInterface {

    void registerShortcut(KeyCodeCombination combination, Consumer<Note> callback);

    void registerType(String typeName, Class<? extends Note> type);

    void registerDataProvider(DataPlugin dataProvider);

    void registerViewer(ViewerPlugin viewer);

    void registerEditor(EditorPlugin editor);

    void registerModule(ModulePlugin module);

}
