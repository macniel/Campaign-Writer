package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Scene {

    UUID location;
    List<UUID> actors;
    String shortDescription;
    String longDescription;


    public List<UUID> getActors() {
        return actors;
    }

    public UUID getLocation() {
        return location;
    }

    public String getLongDescription() {
        return longDescription;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setActors(List<UUID> actors) {
        this.actors = actors;
    }

    public void setLocation(UUID location) {
        this.location = location;
    }

    public void setLongDescription(String longDescription) {
        this.longDescription = longDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }
}
