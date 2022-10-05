package de.macniel.campaignwriter.SDK;

public class FileAccessLayerFactory {

    private static FileAccessLayerInterface fileAccessLayer;

    public void register(FileAccessLayerInterface fal) {
        FileAccessLayerFactory.fileAccessLayer = fal;
    }

    public FileAccessLayerInterface get() {
        return FileAccessLayerFactory.fileAccessLayer;
    }
}
