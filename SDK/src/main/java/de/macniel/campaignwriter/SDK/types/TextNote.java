package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class TextNote extends Note<Text> {

    private Text content;
    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public String getType() {
        return "text";
    }

    public TextNote() {
        super();
        this.content = new Text();

        this.setLabel("Neuer Text");
        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
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
    public String getContent() {
        return new FileAccessLayerFactory().get().getParser().toJson(content);
    }

    @Override
    public Text getContentAsObject() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = new FileAccessLayerFactory().get().getParser().fromJson(content, Text.class);
    }
}
