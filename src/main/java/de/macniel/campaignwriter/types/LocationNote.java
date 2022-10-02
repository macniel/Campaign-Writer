package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class LocationNote extends Note<Location> {

    private Date lastModifiedDate;
    private Date createdDate;
    private Location content;


    @Override
    public String getType() {
        return "location";
    }


    public LocationNote() {
        super();

        this.content = new Location();
        this.setLabel("Neuer Ort");
        this.setReference(UUID.randomUUID());
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
    }

    @Override
    public void setContent(String content) {
        this.content = FileAccessLayer.getInstance().getParser().fromJson(content, Location.class);
    }

    @Override
    public Location getContentAsObject() {
        return content;
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
        return FileAccessLayer.getInstance().getParser().toJson(content);
    }
}
