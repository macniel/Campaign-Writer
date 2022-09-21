package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.NoteType;

public class ActorNoteItem {

    public enum ActorNoteItemType {
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

    public void setLabel(String label) {
        this.label = label;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public void setType(ActorNoteItemType type) {
        this.type = type;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public ActorNoteItemType getType() {
        return type;
    }

    public Integer getMax() {
        return max;
    }

    public Integer getValue() {
        return value;
    }

    public String getContent() {
        return content;
    }
}
