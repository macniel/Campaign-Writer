package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.*;
import javafx.scene.input.KeyCodeCombination;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Consumer;

public class Registry implements RegistryInterface {

    private ArrayList<EditorPlugin> editors = new ArrayList<>();

    private ArrayList<ViewerPlugin> viewers = new ArrayList<>();

    private ArrayList<DataPlugin> dataProviders = new ArrayList<>();
    private ArrayList<ModulePlugin> modules = new ArrayList<>();
    private HashMap<String, Class<? extends Note>> noteTypes = new HashMap<>();

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
    public void registerType(String typeName, Class<? extends Note> type) {
        try {
            type.getDeclaredConstructor().newInstance();
            noteTypes.put(typeName, type);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void registerDataProvider(DataPlugin dataProvider) {
        this.dataProviders.add(dataProvider);
    }

    @Override
    public void registerViewer(ViewerPlugin viewer) {
        viewers.add(viewer);
    }

    @Override
    public void registerEditor(EditorPlugin editor) {
        editors.add(editor);
    }

    @Override
    public void registerModule(ModulePlugin viewer) {
        modules.add(viewer);
    }

    public ArrayList<EditorPlugin> getEditorsByPrefix (String prefix) {
        return new ArrayList<>(editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().startsWith(prefix)).toList());
    }

    public Optional<EditorPlugin> getEditorByFullName (String fullName) {
        return editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().equals(fullName)).findFirst();
    }

    public Optional<ViewerPlugin> getViewerByFullName (String fullName) {
        return viewers.stream().filter(viewerPlugin -> viewerPlugin.defineHandler().equals(fullName)).findFirst();
    }

    public Optional<ViewerPlugin> getViewerBySuffix (String suffix) {
        return viewers.stream().filter(viewerPlugin -> viewerPlugin.defineHandler().endsWith(suffix)).findFirst();
    }

    public ArrayList<ModulePlugin> getAllModules() {
        System.out.println("Modules " + modules.size());
        return modules;
    }

    public HashMap<String, Class<? extends Note>> getNoteTypes() {
        return noteTypes;
    }

    public ArrayList<DataPlugin> getAllDataProviders() {
        return dataProviders;
    }
}
