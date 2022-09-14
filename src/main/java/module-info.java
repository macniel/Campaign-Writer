module de.macniel.campaignwriter {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires com.google.gson;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;

    opens de.macniel.campaignwriter to javafx.fxml;
    exports de.macniel.campaignwriter;
    exports de.macniel.campaignwriter.editors;

    opens de.macniel.campaignwriter.adapters to com.google.gson;
    opens de.macniel.campaignwriter.editors to com.google.gson, javafx.fxml;

}