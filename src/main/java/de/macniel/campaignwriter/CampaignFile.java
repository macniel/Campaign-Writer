package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.CampaignFileInterface;
import de.macniel.campaignwriter.SDK.Note;

import java.util.*;

public class CampaignFile implements CampaignFileInterface {

    public List<Note<?>> notes;

    public Map<String, String> base64Assets;

    public Properties settings;

    public CampaignFile() {
        notes = new ArrayList<>();
        base64Assets = new HashMap<>();
        settings = new Properties();
    }

    @Override
    public List<Note<?>> getNotes() {
        return notes;
    }

    @Override
    public Map<String, String> getBase64Assets() {
        return base64Assets;
    }

    @Override
    public Properties getSettings() {
        return settings;
    }
}
