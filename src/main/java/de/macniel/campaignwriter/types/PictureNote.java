package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class PictureNote extends Note<Picture> {

    @Override
    public String getType() {
        return "picture";
    }

    public PictureNote() {
        super();

        this.content = new Picture();
        this.label = "Neues Bild";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;
    }
}
