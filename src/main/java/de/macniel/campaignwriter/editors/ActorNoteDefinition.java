package de.macniel.campaignwriter.editors;

import java.util.ArrayList;

public class ActorNoteDefinition {

    ArrayList<ActorNoteItem> items;

    public ActorNoteDefinition() {
        items = new ArrayList<>();
    }

    public ArrayList<ActorNoteItem> getItems() {
        return items;
    }

}
