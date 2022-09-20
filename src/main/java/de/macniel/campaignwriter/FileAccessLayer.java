package de.macniel.campaignwriter;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import javafx.scene.image.Image;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;

public class FileAccessLayer {

    private CampaignFile file;

    private Gson gsonParser;
    {
        gsonParser = new Gson();
    }

    private static FileAccessLayer instance;
    public static FileAccessLayer getInstance() {
        if (instance == null) {
            instance = new FileAccessLayer();
        }
        return instance;
    }

    private FileAccessLayer() {
        file = new CampaignFile();
        file.base64Assets = new HashMap<String, String>();
        file.notes = new ArrayList<>();
    }

    public void loadFromFile(File f) throws IOException {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(f));
            if (gsonParser == null) {
                gsonParser = new Gson();
            }
            file = gsonParser.fromJson(reader, CampaignFile.class);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert reader != null;
            reader.close();
        }
    }



    public Map.Entry<String, Image> getImageFromString(String s) {
        try {
            // TODO: lookup filename S campaign, else load it from file referenced by absolutePath s, make a base64 copy and store it in campaign and return image alongside new internal filename
            File f = new File(s);
            String uuid = UUID.randomUUID().toString();
            byte[] input = Files.readAllBytes(Paths.get(s));
            String base64Asset = new String(Base64.getEncoder().encode(input));
            InputStream in = Base64.getDecoder().wrap(new StringBufferInputStream(base64Asset));
            Image image = new Image(in);
            System.out.println("Storing Image from path " + s + " as UUID " + uuid);
            file.base64Assets.put(uuid, base64Asset);
            return new AbstractMap.SimpleEntry<>(uuid, image);
        } catch (FileNotFoundException | NoSuchFileException e) {
            System.out.print("File not found, trying to lookup instead " + s + " ... ");
            String base64Asset = file.base64Assets.get(s);
            InputStream in = Base64.getDecoder().wrap(new StringBufferInputStream(base64Asset));
            Image image = new Image(in);
            System.out.println("found " + image);
            return new AbstractMap.SimpleEntry<>(s, image);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToFile(File f) throws IOException {
        JsonWriter writer = null;
        try {
            writer = new JsonWriter(new FileWriter(f));
            if (gsonParser == null) {
                gsonParser = new Gson();
            }
            gsonParser.toJson(file, CampaignFile.class, writer);

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert writer != null;
            writer.flush();
            writer.close();
        }
    }

    public CampaignFile getFile() {
        return file;
    }

    public void newCampaign() {
        file = new CampaignFile();
    }


    private Note findByReference(UUID ref) {
        System.out.println("searching for " + ref);
        return file.notes.stream().filter( note -> {
            System.out.println(note.reference);
            return note.getReference().equals(ref);

        } ).findFirst().get();
    }

    public void removeNote(Note selectedNote) {
        if (findByReference(selectedNote.getReference()) != null) {
            file.notes.remove(selectedNote);
        }
    }

    public void removeAll() {
        file.notes.clear();
    }

    public List<Note> getAllNotes() {
        return file.notes;
    }
}
