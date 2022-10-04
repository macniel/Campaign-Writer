package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class CombatantNote extends Note<Combatant> {

    private Combatant content;
    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public Combatant getContentAsObject() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = FileAccessLayer.getInstance().getParser().fromJson(content, Combatant.class);
    }

    public String getContent() {
        return FileAccessLayer.getInstance().getParser().toJson(content);
    }

    @Override
    public String getType() {
        return "combatant";
    }

    public CombatantNote() {
        this.content = new Combatant();
        this.setLabel("Neuer Beteiligter");
        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.content.items = new ArrayList<>();
    }

    public void setContentFromObject(Combatant obj) {
        this.content = obj;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public int getLevel() {
        return level;
    }

}
