package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.NoteType;

public class ActorNoteItem {

    enum ActorNoteItemType {
        HEADER("Überschrift"),
        TEXT("Text"),
        RESOURCE("Resource"),
        IMAGE("Bild"),

        STRING("Zeichen");

        public final String label;
        ActorNoteItemType(String label) {
            this.label = label;
        }
        ActorNoteItemType valueOfLabel(String label) {
            for (ActorNoteItemType e: values()) {
                if (e.label.equals(label)) {
                    return e;
                }
            }
            return null;
        }
    }


    ActorNoteItemType type;

    String label;

    String content;

    Integer max;

    Integer value;

}
