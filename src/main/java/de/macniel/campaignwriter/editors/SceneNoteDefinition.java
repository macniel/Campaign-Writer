package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SceneNoteDefinition {

    Note location;
    List<UUID> actors;
    String shortDescription;
    String longDescription;

    public SceneNoteDefinition() {
        location = null;
        actors = new ArrayList<>();
        shortDescription = "";
        longDescription = "";
    }

}
