package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class PictureNote extends Note<Picture> {

    private Picture content;
    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public String getType() {
        return "picture";
    }

    public PictureNote() {
        super();

        this.content = new Picture();
        this.setLabel("Neues Bild");
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
    public Picture getContentAsObject() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = new FileAccessLayerFactory().get().getParser().fromJson(content, Picture.class);
    }
}
