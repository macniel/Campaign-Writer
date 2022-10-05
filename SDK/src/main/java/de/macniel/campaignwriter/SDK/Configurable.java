package de.macniel.campaignwriter.SDK;

import javafx.scene.control.Tab;

import java.util.function.Consumer;

public interface Configurable {

    /**
     * @return the string will be displayed as a Tab Title
     */
    String getConfigMenuItem();

    /**
     * Entry Method to configure the plugin. It has access to the note system and the registry to do its configuration.
     *
     * @param fileAccessLayer the interface to load and save settings either on file or global layer.
     * @param registry        To access other Plugins use this interface
     * @param parent          The Tab the settings should be placed on
     * @return A Consumer-Callback that will be triggered once the Setting Display is closed.
     * the parameter is true if the Setting should be saved or false if the Setting shouldn't be saved.
     */
    Consumer<Boolean> startConfigureTask(FileAccessLayerInterface fileAccessLayer, RegistryInterface registry, Tab parent);

}
