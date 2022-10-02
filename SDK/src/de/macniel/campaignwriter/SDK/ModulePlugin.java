package de.macniel.campaignwriter.SDK;

import java.util.UUID;

import javafx.util.Callback;

public abstract class ModulePlugin implements Registrable {


    public static String getLocalizationBase() {
        return "";
    };

    public abstract String defineViewerHandlerPrefix();

    abstract public String getPathToFxmlDefinition();

    abstract public String getMenuItemLabel();

    abstract public void requestLoad(CampaignFileInterface items);

    abstract public void requestSave();

    public abstract void requestNote(Callback<UUID, Note> cb);

}
