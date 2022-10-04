package de.macniel.campaignwriter.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.paint.Color;

import java.io.IOException;

public class ColorAdapter extends TypeAdapter<Color> {


    @Override
    public void write(JsonWriter out, Color value) throws IOException {
        out.jsonValue(value.toString());
    }

    @Override
    public Color read(JsonReader in) throws IOException {
        return Color.valueOf(in.nextString());
    }
}