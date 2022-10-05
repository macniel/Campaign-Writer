package de.macniel.campaignwriter.SDK.types;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class SessionNote extends Note<Session> {

    private Session content;
    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public String getType() {
        return "session";
    }

    public SessionNote() {
        super();

        this.content = new Session();
        this.setReference(UUID.randomUUID());

        this.setLabel("Neue Sitzung");
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
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
        return new FileAccessLayerFactory().get().getParser().toJson(content);
    }


    @Override
    public void setContent(String content) {
        this.content = new FileAccessLayerFactory().get().getParser().fromJson(content, Session.class);
    }

    @Override
    public Session getContentAsObject() {
        return content;
    }

}
