package de.macniel.campaignwriter.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EncounterNote {

    List<Combatant> combatants;

    UUID encounterLocation;

    String circumstances;

    UUID reference;

    String encounterName;

    String encounterDifficulty;

    public EncounterNote() {
        combatants = new ArrayList<>();
        reference = UUID.randomUUID();
    }

    public void setCircumstances(String circumstances) {
        this.circumstances = circumstances;
    }

    public void setCombatants(List<Combatant> combatants) {
        this.combatants = combatants;
    }

    public void setEncounterDifficulty(String encounterDifficulty) {
        this.encounterDifficulty = encounterDifficulty;
    }

    public void setEncounterLocation(UUID encounterLocation) {
        this.encounterLocation = encounterLocation;
    }

    public void setEncounterName(String encounterName) {
        this.encounterName = encounterName;
    }

    public List<Combatant> getCombatants() {
        return combatants;
    }

    public String getCircumstances() {
        return circumstances;
    }

    public String getEncounterDifficulty() {
        return encounterDifficulty;
    }

    public String getEncounterName() {
        return encounterName;
    }

    public UUID getEncounterLocation() {
        return encounterLocation;
    }

    @Override
    public String toString() {
        return encounterName;
    }

    public UUID getReference() {
        return reference;
    }
}
