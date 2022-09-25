package de.macniel.campaignwriter.editors;

import com.google.gson.Gson;
import de.macniel.campaignwriter.FileAccessLayer;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Combatant {

    public ArrayList<ActorNoteItem> items;

    public Color teamColor;

    public String initiative;

    public static Combatant fromActor(UUID actorRef) {
        AtomicReference<Combatant> newCombatant = new AtomicReference<>();
        FileAccessLayer.getInstance().findByReference(actorRef).ifPresent(actor -> {
            Combatant tmp = new Combatant();
            ActorNoteDefinition def = FileAccessLayer.getInstance().getParser().fromJson(actor.content, ActorNoteDefinition.class);
            tmp.teamColor = Color.GRAY;
            tmp.items = new ArrayList<>(def.items);
            newCombatant.set(tmp);
        });
        return newCombatant.get();
    }

    public Combatant() {
        this.items = new ArrayList<>();
    }

	public ArrayList<ActorNoteItem> getItems() {
		return items;
	}

    public Color getTeamColor() {
        return teamColor;
    }

    public String getInitiative() {
        return initiative;
    }

    public void setInitiative(String initiative) {
        this.initiative = initiative;
    }

    public void setTeamColor(Color teamColor) {
        this.teamColor = teamColor;
    }

}
