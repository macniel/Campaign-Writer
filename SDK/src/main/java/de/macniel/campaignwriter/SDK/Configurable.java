package de.macniel.campaignwriter.SDK;

import javafx.scene.control.Tab;

import java.util.function.Consumer;

public interface Configurable {

    String getConfigMenuItem();

    Consumer<Boolean> startConfigureTask(FileAccessLayerInterface fileAccessLayer, RegistryInterface registry, Tab parent);

}
