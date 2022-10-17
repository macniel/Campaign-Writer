package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.*;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.input.KeyCodeCombination;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Consumer;

public class Registry implements RegistryInterface {

    private static final Registry instance = new Registry();
    private final ArrayList<EditorPlugin> editors = new ArrayList<>();
    private final ArrayList<ViewerPlugin> viewers = new ArrayList<>();
    private final ArrayList<DataPlugin> dataProviders = new ArrayList<>();
    private final ArrayList<ModulePlugin> modules = new ArrayList<>();
    private final HashMap<String, Class<? extends Note>> noteTypes = new HashMap<>();
    private final HashMap<KeyCodeCombination, Consumer<Note>> shortcuts = new HashMap<>();

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

    public ArrayList<EditorPlugin> getEditorsByPrefix(String prefix) {
        return new ArrayList<>(editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().startsWith(prefix)).toList());
    }

    public Optional<EditorPlugin> getEditorByFullName(String fullName) {
        return editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().equals(fullName)).findFirst();
    }

    public Optional<ViewerPlugin> getViewerFor(Note note) {
        List<ViewerPlugin> fittingViewers = viewers.stream().filter(viewerPlugin -> viewerPlugin.defineHandler().endsWith(note.getType())).toList();

        if (note.getDefaultViewer() != null && getViewerByFullName(note.getDefaultViewer()).isPresent()) {
            return getViewerByFullName(note.getDefaultViewer());
        }
        if (fittingViewers.size() == 1) {
            return Optional.of(fittingViewers.get(0));
        } else if (fittingViewers.size() > 1) {
            ChoiceDialog<ViewerPlugin> dlg = new ChoiceDialog<>();
            dlg.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
            dlg.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.APPLY) {
                    System.out.println("Set viewer for this note to " + dlg.getSelectedItem());
                    note.setDefaultViewer(dlg.getSelectedItem().defineHandler());
                    return dlg.getSelectedItem();
                }
                return dlg.getSelectedItem();
            });
            dlg.setTitle("Open Note with the following Viewer");
            dlg.getItems().addAll(fittingViewers);
            Optional<ViewerPlugin> returnValue = dlg.showAndWait();

            return returnValue;
        }
        return Optional.empty();

    }

    public Optional<EditorPlugin> getEditorBySuffix(String suffix) {
        return editors.stream().filter(editorPlugin -> editorPlugin.defineHandler().endsWith(suffix)).findFirst();
    }

    public Optional<ViewerPlugin> getViewerByFullName(String fullName) {
        return viewers.stream().filter(viewerPlugin -> viewerPlugin.defineHandler().equals(fullName)).findFirst();
    }

    public Optional<ViewerPlugin> getViewerBySuffix(String suffix) {
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

    public ArrayList<EditorPlugin> getAllEditors() {
        return editors;
    }

    public ArrayList<ViewerPlugin> getAllViewers() {
        return viewers;
    }

    public ArrayList<Configurable> getAllConfigurables() {
        HashSet<Configurable> returnValue = new HashSet<>();
        returnValue.addAll(getAllDataProviders().stream().filter(c -> c instanceof Configurable).map(c -> (Configurable) c).toList());
        returnValue.addAll(getAllModules().stream().filter(c -> c instanceof Configurable).map(c -> (Configurable) c).toList());
        returnValue.addAll(getAllEditors().stream().filter(c -> c instanceof Configurable).map(c -> (Configurable) c).toList());
        returnValue.addAll(getAllViewers().stream().filter(c -> c instanceof Configurable).map(c -> (Configurable) c).toList());

        return new ArrayList<Configurable>(returnValue);
    }
}
