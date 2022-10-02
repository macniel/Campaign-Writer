package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.Note;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Combatant {

    public ArrayList<ActorNoteItem> items;

    public Color teamColor;

    public String initiative;

    public static Combatant fromActor(UUID actorRef) {
        AtomicReference<Combatant> newCombatant = new AtomicReference<>();
        FileAccessLayer.getInstance().findByReference(actorRef).ifPresent( actor -> {
            Combatant tmp = new Combatant();
            tmp.teamColor = Color.GRAY;

            tmp.items = new ArrayList<>( ((ActorNote)actor).getContentAsObject().getItems());
            newCombatant.set(tmp);
        });
        return newCombatant.get();
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
