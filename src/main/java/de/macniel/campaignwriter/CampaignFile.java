package de.macniel.campaignwriter;

import java.util.*;

public class CampaignFile {

    public List<Note> notes;

    public Map<String, String> base64Assets;

    public CampaignFile() {
        notes = new ArrayList<>();
        base64Assets = new HashMap<>();
    }

}
