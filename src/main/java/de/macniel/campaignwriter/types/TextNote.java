package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class TextNote extends Note<Text> {

    @Override
    public String getType() {
        return "text";
    }

    public TextNote() {
        super();
        this.content = new Text();

        this.label = "Neuer Text";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;

    }
}
