package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;

import java.io.File;
import java.util.Date;
import java.util.UUID;

public class MapNote extends Note<Map> {

    private Date lastModifiedDate;
    private Date createdDate;
    private Map content;

    @Override
    public String getType() {
        return "map";
    }

    public MapNote() {
        super();

        this.content = new Map();
        this.setLabel("Neue Umgebung");
        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
    }

    @Override
    public void setContent(String content) {
        this.content = FileAccessLayer.getInstance().getParser().fromJson(content, Map.class);
    }

    @Override
    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public Date getCreatedDate() {
        return createdDate;
    }


    @Override
    public String getContent() {
        return FileAccessLayer.getInstance().getParser().toJson(content);
    }

    @Override
    public Map getContentAsObject() {
        return content;
    }
}
