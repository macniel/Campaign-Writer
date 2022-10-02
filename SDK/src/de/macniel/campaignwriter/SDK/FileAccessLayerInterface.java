package de.macniel.campaignwriter.SDK;

import com.google.gson.Gson;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

// FIXME: use Factory instead so that plugins can actually call FAL
public interface FileAccessLayerInterface {

    void updateGlobal(String key, String value);

    void loadFromFile(File f) throws IOException;

    void saveToFile(File f) throws IOException;

    Gson getParser();

    CampaignFileInterface getFile();



    String getSetting(String key);

    Optional<Map.Entry<String, Image>> getImageFromString(String s);

}
