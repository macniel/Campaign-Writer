package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;

import java.util.Date;
import java.util.UUID;

public class Picture {

    double zoomFactor;

    String fileName;


    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public double getZoomFactor() {
        return zoomFactor;
    }

    public String getFileName() {
        return fileName;
    }
}
