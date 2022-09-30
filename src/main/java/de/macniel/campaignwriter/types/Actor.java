package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Actor {

    ArrayList<ActorNoteItem> items;

    public ArrayList<ActorNoteItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<ActorNoteItem> merged) {
        this.items = merged;
    }
}
