package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Combatant {

    static Gson gsonParser = new Gson();

    public ArrayList<ActorNoteItem> items;

    public String initiative;

    public static Combatant fromActor(UUID actorRef) {
        AtomicReference<Combatant> newCombatant = new AtomicReference<>();
        FileAccessLayer.getInstance().findByReference(actorRef).ifPresent(actor -> {
            System.out.println("Cloning Actor " + actor.label);
            Combatant tmp = new Combatant();
            ActorNoteDefinition def = gsonParser.fromJson(actor.content, ActorNoteDefinition.class);

            tmp.items = new ArrayList<>(def.items);
            newCombatant.set(tmp);
        });
        return newCombatant.get();
    }

    public Combatant() {
        this.items = new ArrayList<>();
    }

}
