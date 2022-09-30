package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class MapNote extends Note<Map> {

    @Override
    public String getType() {
        return "map";
    }

    public MapNote() {
        super();

        this.content = new Map();
        this.label = "Neue Umgebung";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;
    }
}
