package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class CombatantNote extends Note<Combatant> {

    @Override
    public String getType() {
        return "combatant";
    }

    public CombatantNote() {
        this.content = new Combatant();
        this.label = "Neuer Beteiligter";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;
        this.content.items = new ArrayList<>();
    }

}
