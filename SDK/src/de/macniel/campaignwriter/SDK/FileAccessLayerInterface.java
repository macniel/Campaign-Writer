package de.macniel.campaignwriter.SDK;

import com.google.gson.Gson;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public interface FileAccessLayerInterface {

    public void registerClass(Class c);

    public Gson getParser();

    public void updateGlobal(String key, String value);

    public void loadFromFile(File f) throws IOException;

    public void saveToFile(File f) throws IOException;

    public CampaignFileInterface getFile();



    public String getSetting(String key);

    public Optional<Map.Entry<String, Image>> getImageFromString(String s);

}
