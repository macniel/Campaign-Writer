module CampaignWriter.SDK {
    requires javafx.graphics;
    requires com.google.gson;

    exports de.macniel.campaignwriter.SDK;
    opens de.macniel.campaignwriter.SDK to com.google.gson;
}