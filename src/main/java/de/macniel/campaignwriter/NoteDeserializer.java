package de.macniel.campaignwriter;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionConfig;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.types.ActorNote;
import de.macniel.campaignwriter.types.ActorNoteItem;
import de.macniel.campaignwriter.types.Scene;
import de.macniel.campaignwriter.types.SceneNote;

import java.io.IOException;
import java.util.ArrayList;

public class NoteDeserializer extends StdDeserializer<Note> {

    protected NoteDeserializer() {
        this(null);
    }

    protected NoteDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public Note deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {

        JsonNode node = p.readValueAsTree();
        if (node.has("type")) {
            System.out.println("node with uuid " + node.get("reference").asText() + " is read as type " + node.get("type"));

            Class<? extends Note> t = Registry.getInstance().getNoteTypes().get(node.get("type").asText());

            System.out.println(" resulting in class " + t.toString());

            JsonMapper mapper = JsonMapper.builder().build();
            mapper.coercionConfigDefaults().setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
            Note value = mapper.treeToValue(node, t);

            return value;
        }

        return null;
    }
}
