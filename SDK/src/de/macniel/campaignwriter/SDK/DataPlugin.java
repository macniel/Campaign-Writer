package de.macniel.campaignwriter.SDK;

import javafx.stage.Stage;
import javafx.util.Callback;

public interface DataPlugin extends Registrable {

    String menuItemLabel();

    void startTask(CampaignFileInterface on, Stage parentWnd);

    void setOnFinishedTask(Callback<CampaignFileInterface, Boolean> cb);

}
