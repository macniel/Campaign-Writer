package de.macniel.campaignwriter.SDK;

import com.google.gson.Gson;
import de.macniel.campaignwriter.SDK.types.Actor;
import de.macniel.campaignwriter.SDK.types.SessionNote;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.*;

// FIXME: use Factory instead so that plugins can actually call FAL
public interface FileAccessLayerInterface {

    void updateGlobal(String key, String value);

    void loadFromFile(File f) throws IOException;

    void saveToFile(File f) throws IOException;

    Gson getParser();

    CampaignFileInterface getFile();

    String getSetting(String key);

    Optional<Map.Entry<String, Image>> getImageFromString(String s);

    Optional<Note> findByReference(UUID ref);

    List<Note> getAllNotes();

    HashMap<String, Actor> getTemplates();

    void saveToFile() throws IOException;

    void updateSetting(String lastNote, String toString);

    void addNote(int dragPosition, Note newNote);

    void removeNote(Note note);

    Optional<String> getGlobal(String key);

    void newCampaign();
}
