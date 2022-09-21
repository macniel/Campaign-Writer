package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;

import java.util.ArrayList;
import java.util.List;

public class EncounterNote {

    List<Note> combatants;

    Note encounterLocation;

    String circumstances;

    String encounterName;

    String encounterDifficulty;

    public EncounterNote() {
        combatants = new ArrayList<>();
    }

}
