package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class Location {

    UUID parentLocation;

    String name;

    UUID picture;

    String canonicalName;

    String ambiance;

    String description;

    String history;

    public String getAmbiance() {
        return ambiance;
    }

    public String getCanonicalName() {
        return canonicalName;
    }
    public String getDescription() {
        return description;
    }

    public String getHistory() {
        return history;
    }
    public String getName() {
        return name;
    }
    public UUID getParentLocation() {
        return parentLocation;
    }
    public UUID getPicture() {
        return picture;
    }
    public void setAmbiance(String ambiance) {
        this.ambiance = ambiance;
    }
    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setHistory(String history) {
        this.history = history;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setParentLocation(UUID parentLocation) {
        this.parentLocation = parentLocation;
    }
    public void setPicture(UUID picture) {
        this.picture = picture;
    }
}
