package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.CampaignFile;
import de.macniel.campaignwriter.Note;
import de.macniel.campaignwriter.editors.EncounterNote;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EncounterView  implements ViewInterface {
    private Callback<UUID, Note> requester;

    List<EncounterNote> encounters;

    @Override
    public String getPathToFxmlDefinition() {
        return "encounter-view.fxml";
    }

    @Override
    public String getMenuItemLabel() {
        return "Begegnung";
    }

    @Override
    public void requestLoad(CampaignFile items) {
        encounters = items.encounterNotes;
    }

    @Override
    public void requestSave() {

    }

    @Override
    public void requestNote(Callback<UUID, Note> cb) {
        this.requester = cb;
    }
}
