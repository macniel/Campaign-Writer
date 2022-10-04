package de.macniel.campaignwriter.types;

import de.macniel.campaignwriter.SDK.Note;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Map {

    public double scale;
    ArrayList<MapPin> pins;

    public String backgroundPath;

    double scrollPositionX;

    double scrollPositionY;

    double zoomFactor;

    ArrayList<Point2D> fog = new ArrayList<>();

    public double getZoomFactor() {
        return zoomFactor;
    }

    public ArrayList<MapPin> getPins() {
        return pins;
    }

    public double getScale() {
        return scale;
    }

    public double getScrollPositionX() {
        return scrollPositionX;
    }

    public double getScrollPositionY() {
        return scrollPositionY;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public void setBackgroundPath(String backgroundPath) {
        this.backgroundPath = backgroundPath;
    }

    public void setPins(ArrayList<MapPin> pins) {
        this.pins = pins;
    }

    public void setScale(double scale) {
        this.scale = scale;
    }

    public void setScrollPositionX(double scrollPositionX) {
        this.scrollPositionX = scrollPositionX;
    }

    public void setScrollPositionY(double scrollPositionY) {
        this.scrollPositionY = scrollPositionY;
    }

    public ArrayList<Point2D> getFog() {
        return fog;
    }

    public void setFog(ArrayList<Point2D> fog) {
        this.fog = fog;
    }

    public void setZoomFactor(double zoomFactor) {
        this.zoomFactor = zoomFactor;
    }
}
