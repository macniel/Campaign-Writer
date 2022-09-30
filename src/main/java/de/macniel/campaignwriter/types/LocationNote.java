package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class LocationNote extends Note<Location> {

    @Override
    public String getType() {
        return "location";
    }

    public LocationNote() {
        super();

        this.content = new Location();
        this.label = "Neuer Ort";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;

    }
}
