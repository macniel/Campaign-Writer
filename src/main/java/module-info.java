module de.macniel.campaignwriter {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires org.apache.commons.imaging;
    requires SDK;
    requires org.reflections;
    requires com.fasterxml.jackson.dataformat.xml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;


    opens de.macniel.campaignwriter to javafx.fxml;
    opens de.macniel.campaignwriter.modules to javafx.fxml;

    exports de.macniel.campaignwriter;
    exports de.macniel.campaignwriter.editors;

    opens de.macniel.campaignwriter.adapters to com.google.gson;
    opens de.macniel.campaignwriter.editors to com.google.gson, javafx.fxml;
    exports de.macniel.campaignwriter.types;
    opens de.macniel.campaignwriter.types to com.google.gson, javafx.fxml;

}