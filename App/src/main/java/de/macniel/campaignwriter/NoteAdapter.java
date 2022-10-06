package de.macniel.campaignwriter;

import com.google.gson.*;
import de.macniel.campaignwriter.SDK.Note;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.UUID;

public class NoteAdapter implements JsonDeserializer<Note>, JsonSerializer<Note> {

    @Override
    public Note deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject o = json.getAsJsonObject();
        if (o.has("type")) {
            Registry.getInstance().getNoteTypes().keySet().forEach(k -> System.out.println("  " + k));
            Class<Note> n = (Class<Note>) Registry.getInstance().getNoteTypes().get(o.get("type").getAsString());
            System.out.println(o.get("type").getAsString());
            try {
                Note note = n.getDeclaredConstructor().newInstance();
                note.setLabel(o.get("label").getAsString());
                note.setContent(o.get("content").getAsString());
                note.setReference(UUID.fromString(o.get("reference").getAsString()));
                note.setLevel(o.get("level").getAsInt());
                return note;

            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException ignored) {
            }
        }

        return null;
    }

    @Override
    public JsonElement serialize(Note src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject target = new JsonObject();

        target.addProperty("type", src.getType());
        target.addProperty("content", src.getContent());
        target.addProperty("label", src.getLabel());
        target.addProperty("reference", src.getReference().toString());
        target.addProperty("level", src.getLevel());


        return target;
    }
}
