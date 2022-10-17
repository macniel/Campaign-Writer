package de.macniel.campaignwriter;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SettingsDialog {

    public void show(Window parent) {

        Stage wnd = new Stage();

        BorderPane bp = new BorderPane();

        ButtonBar controls = new ButtonBar();
        controls.setPadding(new Insets(10));

        TabPane pane = new TabPane();
        pane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);

        ArrayList<Consumer<Boolean>> registeredCallbacks = new ArrayList<>();

        generalTab(pane);

        pluginTab(pane);

        Registry.getInstance().getAllConfigurables().forEach(configurable -> {

            Tab tab = new Tab();
            tab.setText(configurable.getConfigMenuItem());
            try {
                Consumer<Boolean> callback = configurable.startConfigureTask(FileAccessLayer.getInstance(), Registry.getInstance(), tab);
                registeredCallbacks.add(callback);
            } catch (AbstractMethodError e) {
                tab.setContent(new Label("Error while loading settings of Plugin `" + configurable.getClass().getSimpleName() + "` perhaps older version?"));
            }
            tab.setClosable(false);
            pane.getTabs().add(tab);
        });

        Button ok = new Button("ok");
        Button cancel = new Button("cancel");

        ok.onActionProperty().set(e -> {
            registeredCallbacks.forEach(callback -> callback.accept(true));
            wnd.close();
        });

        cancel.onActionProperty().set(e -> {
            registeredCallbacks.forEach(callback -> callback.accept(false));
            wnd.close();
        });

        controls.getButtons().addAll(ok, cancel);

        bp.setCenter(pane);

        bp.setBottom(controls);

        wnd.setScene(new Scene(bp, 400, 300));

        wnd.setTitle("Settings of Campaign Writer");

        wnd.initOwner(parent);
        wnd.initModality(Modality.APPLICATION_MODAL);
        wnd.showAndWait();

    }

    private void generalTab(TabPane pane) {
        Tab generalTab = new Tab();
        generalTab.setClosable(false);
        generalTab.setText("General");

        // languages and such
        VBox box = new VBox();

        HBox resetDefaultViewer = new HBox();
        resetDefaultViewer.setSpacing(5);
        Label l = new Label("Viewer association");
        resetDefaultViewer.setAlignment(Pos.CENTER);
        Button o = new Button("reset");
        o.onActionProperty().set(e -> {
            FileAccessLayer.getInstance().getAllNotes().forEach(note -> {

                note.setDefaultViewer("");
            });
            new Alert(Alert.AlertType.INFORMATION, "All Notes were reset").showAndWait();
        });
        resetDefaultViewer.getChildren().addAll(l, o);

        box.setSpacing(5);
        box.setPadding(new Insets(10));
        box.getChildren().add(resetDefaultViewer);
        generalTab.setContent(box);

        pane.getTabs().add(generalTab);
    }

    private void pluginTab(TabPane pane) {
        Tab pluginTab = new Tab();
        pluginTab.setClosable(false);
        VBox box = new VBox();
        box.getChildren().add(new Label("Loaded Plugins"));
        box.setSpacing(5);
        box.setPadding(new Insets(10));
        ListView<String> pluginView = new ListView<>();

        pluginTab.setText("Plugins");
        HashSet<Class> plugins = new HashSet<>();

        Registry.getInstance().getAllDataProviders().forEach(provider -> {
            plugins.add(provider.getClass());
        });
        Registry.getInstance().getAllModules().forEach(provider -> {
            plugins.add(provider.getClass());
        });
        Registry.getInstance().getAllEditors().forEach(provider -> {
            plugins.add(provider.getClass());
        });
        Registry.getInstance().getAllViewers().forEach(provider -> {
            plugins.add(provider.getClass());
        });

        pluginView.setItems(FXCollections.observableArrayList(plugins.stream().map(Class::getSimpleName).toList()));

        HBox openPluginFolder = new HBox();
        openPluginFolder.setSpacing(5);
        Label l = new Label("Plugin folder");
        openPluginFolder.setAlignment(Pos.CENTER);
        Button o = new Button("open");
        o.onActionProperty().set(e -> {
            FileAccessLayer.getInstance().getHostServices().showDocument(FileAccessLayer.getInstance().getPluginFolderPath());
        });
        openPluginFolder.getChildren().addAll(l, o);

        box.getChildren().addAll(pluginView, openPluginFolder);

        pluginTab.setContent(box);

        pane.getTabs().add(pluginTab);
    }

}
