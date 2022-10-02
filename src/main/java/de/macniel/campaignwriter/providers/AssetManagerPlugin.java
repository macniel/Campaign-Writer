package de.macniel.campaignwriter.providers;

import de.macniel.campaignwriter.FileAccessLayer;
import de.macniel.campaignwriter.SDK.CampaignFileInterface;
import de.macniel.campaignwriter.SDK.DataPlugin;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.util.concurrent.atomic.AtomicInteger;

public class AssetManagerPlugin implements DataPlugin {


    private Callback<CampaignFileInterface, Boolean> callback;

    private CampaignFileInterface file;

    @Override
    public String menuItemLabel() {
        return "Asset Manager";
    }

    @Override
    public void startTask(CampaignFileInterface on, Stage parentWnd) {
        file = on;
        Stage wnd = new Stage();

        BorderPane borderPane = new BorderPane();
        Button deleteAsset = new Button("Delete");
        deleteAsset.setDisable(true);

        ListView<String> view = new ListView();
        view.setItems(FXCollections.observableArrayList(file.getBase64Assets().keySet()));
        borderPane.setLeft(view);

        ImageView viewer = new ImageView();

        borderPane.setCenter(viewer);

        ButtonBar controls = new ButtonBar();

        Button closeButton = new Button("Close");

        HBox status = new HBox();
        HBox.setHgrow(status, Priority.ALWAYS);

        controls.getButtons().addAll(status, deleteAsset, closeButton);

        borderPane.setBottom(controls);

        Scene c = new Scene(borderPane, 200, 200);


        view.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                deleteAsset.setDisable(true);
            }
            FileAccessLayer.getInstance().getImageFromString(newValue).ifPresentOrElse(entry -> {
                viewer.setImage(entry.getValue());
                AtomicInteger refs = new AtomicInteger();
                FileAccessLayer.getInstance().getAllNotes().forEach(note -> {
                    if (note.getContent().contains(newValue)) {
                        refs.getAndIncrement();
                    }
                });
                wnd.setTitle("Image is used in " + (refs.get()==1? "1 Note" : refs.get() + " Notes"));
                deleteAsset.setDisable(false);
            }, () -> {
                deleteAsset.setDisable(true);
            });

        });

        deleteAsset.onActionProperty().set(e -> {
            file.getBase64Assets().remove(view.getSelectionModel().getSelectedItem());
            view.setItems(FXCollections.observableArrayList(file.getBase64Assets().keySet()));
        });

        closeButton.onActionProperty().set(e -> {
            wnd.close();
        });

        wnd.setScene(c);
        wnd.initModality(Modality.APPLICATION_MODAL);
        wnd.initOwner(parentWnd);

        wnd.showAndWait();
        if (this.callback != null) {
            this.callback.call(file);
        }

    }

    @Override
    public void setOnFinishedTask(Callback<CampaignFileInterface, Boolean> cb) {
        this.callback = cb;
    }

    @Override
    public void register(RegistryInterface registry) {
        registry.registerDataProvider(this);
    }
}
