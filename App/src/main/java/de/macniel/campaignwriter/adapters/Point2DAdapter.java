package de.macniel.campaignwriter.adapters;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.geometry.Point2D;

import java.io.IOException;
import java.lang.reflect.Type;

public class Point2DAdapter implements JsonSerializer<Point2D>, JsonDeserializer<Point2D> {

    @Override
    public JsonElement serialize(Point2D src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        o.addProperty("x", src.getX());
        o.addProperty("y", src.getY());
        return o;
    }

    @Override
    public Point2D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject o = json.getAsJsonObject();

        double x = o.get("x").getAsDouble();
        double y = o.get("y").getAsDouble();
        return new Point2D(x, y);
    }
}
