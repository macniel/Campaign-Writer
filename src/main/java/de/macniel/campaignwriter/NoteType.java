package de.macniel.campaignwriter;

public enum NoteType {
    // FIXME: refactor into plugin system
    TEXT_NOTE("Notiz"),
    ACTOR_NOTE("Akteur"),
    LOCATION_NOTE("Ort"),
    SCENE_NOTE("Szene"),
    PICTURE_NOTE("Bild"),
    MAP_NOTE("Landkarte"),
    ENCOUNTER_NOTE("Begegnung"),
    RELATIONSHIP_NOTE("Beziehung"), 
    COMBATANT_NOTE("Gegner");

    public final String label;
    NoteType(String label) {
        this.label = label;
    }
    NoteType valueOfLabel(String label) {
        for (NoteType e: values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }
}
