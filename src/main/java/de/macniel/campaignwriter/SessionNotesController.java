package de.macniel.campaignwriter;

import de.macniel.campaignwriter.editors.SessionNote;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class SessionNotesController {

    private SessionNote note;
    @FXML
    private Text label;

    @FXML
    private HBox view;

    @FXML
    private FontIcon icon;

    public SessionNotesController(SessionNote note) {

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
        if (note != null) {
            this.label.setText(note.getLabel());
        }
    }

    public void setItem(SessionNote note) {
        this.note = note;
        this.label.setText(note.getLabel());
        view.backgroundProperty().set(Background.EMPTY);
        icon.setIconLiteral("icm-quill");
    }

    public Node getView() {
        return view;
    }

    public SessionNote getItem() {
        return note;
    }
}
