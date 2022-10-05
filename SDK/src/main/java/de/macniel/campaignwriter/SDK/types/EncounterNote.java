package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class EncounterNote extends Note<Encounter> {


    private Encounter content;
    private Date createdDate;
    private Date lastModifiedDate;


    public EncounterNote() {
        this.content = new Encounter();
        this.setLabel("Neue Begegnung");

        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.content.combatants = new ArrayList<>();
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getContent() {
        return new FileAccessLayerFactory().get().getParser().toJson(content);
    }

    @Override
    public String getType() {
        return "encounter";
    }

    @Override
    public void setContent(String content) {
        this.content = new FileAccessLayerFactory().get().getParser().fromJson(content, Encounter.class);
    }

    @Override
    public Encounter getContentAsObject() {
        return content;
    }

}
