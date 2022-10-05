module CampaignWriter.SDK {
    requires javafx.graphics;
    requires com.google.gson;
    requires javafx.controls;

    exports de.macniel.campaignwriter.SDK;
    opens de.macniel.campaignwriter.SDK to com.google.gson;
    opens de.macniel.campaignwriter.SDK.types to com.google.gson;
    exports de.macniel.campaignwriter.SDK.types;
}