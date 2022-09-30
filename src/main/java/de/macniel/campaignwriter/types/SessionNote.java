package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SessionNote extends Note<Session> {

    @Override
    public String getType() {
        return "session";
    }

    public SessionNote() {
        super();

        this.content = new Session();

        this.label = "Neue Sitzung";
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;

        this.content.comment = "";
        this.content.notes = new ArrayList<>();
        this.content.played = false;
        this.content.playDate = null;
        this.reference = UUID.randomUUID();
    }
}
