package de.macniel.campaignwriter.views;

import de.macniel.campaignwriter.CampaignFile;
import de.macniel.campaignwriter.Note;
import javafx.util.Callback;

import java.util.UUID;

public abstract class ViewInterface {

    abstract public String getPathToFxmlDefinition();

    public static String getLocalizationBase() {
        return "";
    };

    abstract public String getMenuItemLabel();

    abstract public void requestLoad(CampaignFile items);

    abstract public void requestSave();

    public abstract void requestNote(Callback<UUID, Note> cb);

}
