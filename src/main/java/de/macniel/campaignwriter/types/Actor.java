package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Actor {

    List<ActorNoteItem> items;

    public List<ActorNoteItem> getItems() {
        return items;
    }

    public void setItems(List<ActorNoteItem> merged) {
        this.items = merged;
    }

    public Actor() {
        items = new ArrayList<>();
    }

}
