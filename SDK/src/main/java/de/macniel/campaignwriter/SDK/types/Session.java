package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Session {

    String label;

    List<UUID> notes;

    UUID reference;

    String comment;

    Date playDate;

    Boolean played;

    public Session() {
        this.comment = "";
        this.notes = new ArrayList<>();
        this.played = false;
        this.playDate = null;
    }


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


    public UUID getReference() {
        return reference;
    }
}
