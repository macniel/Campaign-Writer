package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.Note;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
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
            new Scene(parent, 400.0 ,500.0);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }


        this.note = note;
    }

    public void setItem(Note note) {
        if (note != null) {
            view.setPadding(new Insets(0, 0, 0, note.getLevel() * 8));
            this.note = note;
            this.label.setText(note.getLabel());
            view.backgroundProperty().set(Background.EMPTY);
            if (note.getType() != null) {
                switch (note.getType()) {
                    case "location" -> icon.setIconLiteral("icm-location");
                    case "text" -> icon.setIconLiteral("icm-file-text");
                    case "actor" -> icon.setIconLiteral("icm-user");
                    case "map" -> icon.setIconLiteral("icm-map");
                    case "picture" -> icon.setIconLiteral("icm-image");
                    case "scene" -> icon.setIconLiteral("icm-clock");
                    case "relationship" -> icon.setIconLiteral("icm-share2");
                    default -> icon.setIconLiteral("icm-file-text");
                }
            }
        }
    }

    public Node getView() {
        return view;
    }

    public Note getItem() {
        return note;
    }
}
