package de.macniel.campaignwriter.SDK;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;


public abstract class Note<T> implements Serializable {

    public UUID reference;
    public int level;
    public String label;

    abstract public Date getCreatedDate();

    abstract public Date getLastModifiedDate();

    abstract public String getContent();

    abstract public T getContentAsObject();

    abstract public void setContent(String content);

    abstract public String getType();


    public void setReference(UUID reference) {
        this.reference = reference;
    }

    public UUID getReference() {
        return this.reference;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String s) {
        this.label = s;
    }

}
