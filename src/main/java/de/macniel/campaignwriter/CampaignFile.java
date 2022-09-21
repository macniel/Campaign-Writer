package de.macniel.campaignwriter;

import de.macniel.campaignwriter.editors.EncounterNote;
import de.macniel.campaignwriter.editors.SessionNote;

import java.util.*;

public class CampaignFile {

    public List<Note> notes;

    public List<SessionNote> sessionNotes;

    public Map<String, String> base64Assets;
    public List<EncounterNote> encounterNotes;

    public CampaignFile() {
        notes = new ArrayList<>();
        sessionNotes = new ArrayList<>();
        encounterNotes = new ArrayList<>();
        base64Assets = new HashMap<>();
    }

}
