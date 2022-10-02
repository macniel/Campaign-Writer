package de.macniel.campaignwriter.SDK;

import javafx.stage.Stage;
import javafx.util.Callback;

/**
 * Plugin Interface for Data Provider. They are accessible via a menu Item inside the "Data" Menu
 * They are called via startTask and signal that they are done via callback defined in onFinishedTask.
 * <p>
 * Data Plugins work on an actual CampaignFile, so changes need to be done with care.
 */
public interface DataPlugin extends Registrable {

    /**
     * The text will be displayed in the Menu
     * @return
     */
    String menuItemLabel();

    /**
     * The MainController calls this plugin do start the task
     * @param on the actual campaign file
     * @param parentWnd the parent window, use this reference for modal windows
     */
    void startTask(CampaignFileInterface on, Stage parentWnd);

    /**
     * Callback once the task is terminated regardless of it being a success or failure
     * @param cb
     */
    void setOnFinishedTask(Callback<CampaignFileInterface, Boolean> cb);

}
