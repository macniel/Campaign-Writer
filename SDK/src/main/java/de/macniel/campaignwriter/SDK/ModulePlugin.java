package de.macniel.campaignwriter.SDK;

import java.util.UUID;

import javafx.util.Callback;

public abstract class ModulePlugin implements Registrable {

    /**
     * For i18n purposes define a localization base path in which a ResourceBundle resides
     * @return
     */
    public static String getLocalizationBase() {
        return "";
    };

    /**
     * Define the prefix of this Module e.g. building, encounter, session
     * @return
     */
    public abstract String defineViewerHandlerPrefix();

    /**
     * In case this Module is used with a Fxml Definition define it here
     * @return
     */
    abstract public String getPathToFxmlDefinition();

    /**
     * Display text of the menuitem
     * @return
     */
    abstract public String getMenuItemLabel();

    /**
     * Module has been activated and is required to load the Notes from the CampaignFile it can handle
     * @param items
     */
    abstract public void requestLoad(CampaignFileInterface items);

    /**
     * Saving the Contents inside the Module
     */
    abstract public void requestSave();

    /**
     * Load a Note
     * @param cb
     */
    public abstract void requestNote(Callback<UUID, Note> cb);

    public abstract void requestLoadNote(Callback<UUID, Boolean> cb);

}
