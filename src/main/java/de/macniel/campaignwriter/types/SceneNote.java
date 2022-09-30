package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class SceneNote extends Note<Scene> {

    @Override
    public String getType() {
        return "scene";
    }

    public SceneNote() {
        super();
        this.content = new Scene();

        this.label = "Neue Szene";
        this.reference = UUID.randomUUID();
        this.createdDate = new Date();
        this.lastModifiedDate = new Date();
        this.type = getType();
        this.level = 0;

        this.content.location = null;
        this.content.actors = new ArrayList<>();
        this.content.shortDescription = "";
        this.content.longDescription = "";
    }

}
