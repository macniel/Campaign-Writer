package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SceneNote extends Note<Scene> {

    private Scene content;

    private Date createdDate;
    private Date lastModifiedDate;

    @Override
    public String getType() {
        return "scene";
    }

    public SceneNote() {
        super();
        this.content = new Scene();
        System.out.println("corrupt scene refresh");

        this.setLabel("Neue Szene");
        this.setReference(UUID.randomUUID());
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
        return FileAccessLayer.getInstance().getParser().toJson(content);
    }

    @Override
    public void setContent(String content) {
        this.content = FileAccessLayer.getInstance().getParser().fromJson(content, Scene.class);
    }

    @Override
    public Scene getContentAsObject() {
        return content;
    }
}
