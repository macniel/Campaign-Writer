package de.macniel.campaignwriter.SDK;

public interface Configurable {

    String getConfigMenuItem();

    void startConfigureTask(FileAccessLayerInterface fileAccessLayer, RegistryInterface registry);

}
