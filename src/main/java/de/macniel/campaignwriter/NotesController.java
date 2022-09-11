package de.macniel.campaignwriter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class NotesController {

    private Note note;
    @FXML
    private Text label;

    @FXML
    private HBox view;

    @FXML
    private FontIcon icon;

    public NotesController(Note note) {

        FXMLLoader fxmlLoader = new FXMLLoader(CampaignWriterApplication.class.getResource("notes-view.fxml"));
        fxmlLoader.setController(this);
        try
        {
            Parent parent = fxmlLoader.load();
            Scene scene = new Scene(parent, 400.0 ,500.0);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }


        this.note = note;
        if (note != null) {
            this.label.setText(note.getLabel());
        }
    }

    public void setItem(Note note) {
        this.note = note;
        this.label.setText(note.getLabel());
        switch (note.getType()) {
            case LOCATION_NOTE -> icon.setIconLiteral("icm-location");
            case TEXT_NOTE -> icon.setIconLiteral("icm-file-text");
            case ACTOR_NOTE -> icon.setIconLiteral("icm-user");
            case MAP_NOTE -> icon.setIconLiteral("icm-map");
            case PICTURE_NOTE -> icon.setIconLiteral("icm-image");
            case SCENE_NOTE -> icon.setIconLiteral("icm-clock");
            case RELATIONSHIP_NOTE -> icon.setIconLiteral("icm-share2");
        }
    }

    public Node getView() {
        return view;
    }

}
