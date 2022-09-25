package de.macniel.campaignwriter.editors;

import de.macniel.campaignwriter.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SessionNote {

    String label;

    List<UUID> notes;

    UUID reference;

    String comment;

    Date playDate;

    Boolean played;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<UUID> getNotes() {
        return notes;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    public Boolean getPlayed() {
        return played;
    }

    public void setPlayed(Boolean played) {
        this.played = played;
    }

    public Date getPlayDate() {
        return playDate;
    }

    public void setPlayDate(Date playDate) {
        this.playDate = playDate;
    }

    public SessionNote() {
        this.comment = "";
        this.notes = new ArrayList<>();
        this.played = false;
        this.playDate = null;
        this.reference = UUID.randomUUID();
    }

    public UUID getReference() {
        return reference;
    }
}
