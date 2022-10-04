module CampaignWriter.Core {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires CampaignWriter.SDK;
    requires org.kordamp.ikonli.javafx;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.annotation;

    requires com.google.gson;
    requires com.fasterxml.jackson.databind;
    requires org.controlsfx.controls;
    requires org.reflections;

    opens de.macniel.campaignwriter.types to com.google.gson;
    opens de.macniel.campaignwriter;

    exports de.macniel.campaignwriter to javafx.graphics;
    opens de.macniel.campaignwriter.modules;

}