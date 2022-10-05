package de.macniel.campaignwriter.SDK.types;

import javafx.scene.paint.Color;

import java.util.UUID;

public class MapPin {

    double x;
    double y;

    String label;

    Color color;

    UUID noteReference;

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public UUID getNoteReference() {
        return noteReference;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setNoteReference(UUID noteReference) {
        this.noteReference = noteReference;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
