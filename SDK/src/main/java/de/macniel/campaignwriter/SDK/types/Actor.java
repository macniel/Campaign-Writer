package de.macniel.campaignwriter.SDK.types;

import java.util.ArrayList;
import java.util.List;

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
