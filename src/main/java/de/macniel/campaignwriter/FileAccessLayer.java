package de.macniel.campaignwriter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.image.Image;

import java.io.*;
import java.util.ArrayList;

public class FileAccessLayer {

    private static Gson gsonParser;
    {
        gsonParser = new Gson();
    }

    public static void loadFromFile(File f) throws IOException {
        JsonReader reader = null;
        try {
            Note.removeAll();
            reader = new JsonReader(new FileReader(f));
            if (gsonParser == null) {
                gsonParser = new Gson();
            }
            Note[] concrete = gsonParser.fromJson(reader, Note[].class);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert reader != null;
            reader.close();
        }
    }

    public static Image getImageFromString(String s) {
        try {
            File f = new File(s);
            return new Image(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveToFile(File f, ArrayList<Note> notes) throws IOException {
        JsonWriter writer = null;
        try {
            writer = new JsonWriter(new FileWriter(f));
            Note[] concrete = Note.getAll().toArray(new Note[Note.getAll().size()]);
            if (gsonParser == null) {
                gsonParser = new Gson();
            }
            gsonParser.toJson(concrete, Note[].class, writer);

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert writer != null;
            writer.flush();
            writer.close();
        }
    }

}
