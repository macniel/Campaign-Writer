package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ActorNote extends Note<Actor> {

    @Override
    public String getType() {
        return "actor";
    }

    public ActorNote() {
        super();

        this.content = new Actor();
        this.label = "Neuer Akteur";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;
        this.content.items = new ArrayList<>();
    }

}
