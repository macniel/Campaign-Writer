package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class EncounterNote extends Note<Encounter> {

    @Override
    public String getType() {
        return "encounter";
    }

    public EncounterNote() {
        this.content = new Encounter();
        this.label = "Neue Begegnung";

        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;
        this.content.combatants = new ArrayList<>();
        reference = UUID.randomUUID();
    }
}
