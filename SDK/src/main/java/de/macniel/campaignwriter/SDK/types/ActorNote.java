package de.macniel.campaignwriter.SDK.types;

import com.google.gson.Gson;
import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ActorNote extends Note<Actor> {

    private Actor content;

    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public Date getCreatedDate() {
       return createdDate;
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public String getContent() {
        return new Gson().toJson(content);
    }

    @Override
    public Actor getContentAsObject() {
        return content;
    }

    public void setContent(String content) {
        this.content = new Gson().fromJson(content, Actor.class);
    }

    @Override
    public String getType() {
        return "actor";
    }

    public ActorNote() {
        super();

        this.content = new Actor();
        this.setLabel("Neuer Akteur");
        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.content.items = new ArrayList<>();
    }

}
