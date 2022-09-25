package de.macniel.campaignwriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import de.macniel.campaignwriter.adapters.ColorAdapter;
import de.macniel.campaignwriter.editors.ActorNoteDefinition;
import de.macniel.campaignwriter.editors.EncounterNote;
import de.macniel.campaignwriter.editors.SessionNote;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileAccessLayer {

    private CampaignFile file;

    private Gson gsonParser;
    {
        gsonParser = new GsonBuilder().registerTypeAdapter(Color.class, new ColorAdapter()).create();
    }

    private Properties config;
    private File confFile;

    private File keepFile;

    private static FileAccessLayer instance = new FileAccessLayer();
    public static FileAccessLayer getInstance() {
        return instance;
    }

    private FileAccessLayer() {
        file = new CampaignFile();
        file.base64Assets = new HashMap<String, String>();
        file.notes = new ArrayList<>();

  
        initConfFile();
    }

    public Gson getParser() {
        return gsonParser;
    }

    private void initConfFile() {
        try {
            confFile = new File(System.getProperty("user.home") + "/.campaignwriterrc");
            if (!confFile.exists()) {
                confFile.createNewFile();
            }
            this.config = new Properties();
            this.config.load(new FileInputStream(confFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGlobal(String key, String value) {
        if( config == null ) {
            initConfFile();
        }
        try {
            config.setProperty(key, value);
            config.store(new FileOutputStream(confFile), "update global " + key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Optional<String> getGlobal(String key) {
        if( config == null ) {
            initConfFile();
        }

        return Optional.ofNullable(config.getProperty(key));     
          
    }
    

    public void loadFromFile(File f) throws IOException {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(f));
            file = gsonParser.fromJson(reader, CampaignFile.class);
            updateGlobal("lastFilePath", f.getAbsolutePath());
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert reader != null;
            reader.close();
        }
    }

    public Properties getSettings() {
        if (file != null) {
            return file.settings;
        }
        return null;
    }

    public void updateSetting(String key, String newValue) {
        if (file != null) {
            file.settings.setProperty(key, newValue);
            if (keepFile != null) {
                try {
                    saveToFile(keepFile);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public String getSetting(String key) {
        if (file != null) {
            return file.settings.getProperty(key);
        }
        return null;
    }

    public HashMap<String, ActorNoteDefinition> getTemplates() {
    
        File templateDir = Paths.get(System.getProperty("user.home"), "campaignwriter", "templates").toFile();
        if (templateDir.exists() && templateDir.isDirectory()) {
            HashMap<String, ActorNoteDefinition> templates = new HashMap<>();

            for (File f : templateDir.listFiles()) {
                
                ActorNoteDefinition fromFile;
                try {
                    fromFile = gsonParser.fromJson(new JsonReader(new FileReader(f)), ActorNoteDefinition.class);
                    templates.put(f.getName(), fromFile);
                } catch (JsonIOException | JsonSyntaxException | FileNotFoundException e) {
                    System.err.println("Failure to read template file \"" + f.getAbsolutePath() + "\"");
                    e.printStackTrace();
                } 
            }
            return templates;
        } else {
            try {
                templateDir.createNewFile();
            
            templateDir.mkdir();
            return null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Optional<Map.Entry<String, Image>> getImageFromString(String s) {

        if (new File(s).exists()) {
            try {
            String uuid = UUID.randomUUID().toString();
            byte[] input = Files.readAllBytes(Paths.get(s));
            String base64Asset = new String(Base64.getEncoder().encode(input));
            InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Asset.getBytes("UTF-8")));
            Image image = new Image(in);
            System.out.println("Storing Image from path " + s + " as UUID " + uuid);
            file.base64Assets.put(uuid, base64Asset);
            return Optional.ofNullable(new AbstractMap.SimpleEntry<>(uuid, image));
            } catch (Exception e) {}
        }
        try {
            String base64Asset = file.base64Assets.get(s);
            InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Asset.getBytes("UTF-8")));
            Image image = new Image(in);
            return Optional.ofNullable(new AbstractMap.SimpleEntry<>(s, image));
        } catch (Exception e) {}
        return Optional.empty();
    }

    public void saveToFile(File f) throws IOException {
        JsonWriter writer = null;
        keepFile = f;
        try {
            writer = new JsonWriter(new FileWriter(f));
            // TODO: Count Base64 Refs for Garbage Collecting
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

    public Optional<Note> findByLabel(String label) {
        System.out.println("searching for Note with Label " + label);
        return file.notes.stream().filter( note -> {
            System.out.println(note.label);
            return note.getLabel().equals(label);

        } ).findFirst();
    }


    public Optional<Note> findByReference(UUID ref) {
        System.out.println("searching for " + ref);
        return file.notes.stream().filter( note -> {
            System.out.println(note.reference);
            return note.getReference().equals(ref);

        } ).findFirst();
    }

    public void removeNote(Note selectedNote) {
        findByReference(selectedNote.getReference()).ifPresent( note -> {
            file.notes.remove(selectedNote);
        });
    }

    public void removeAll() {
        file.notes.clear();
    }

    public List<Note> getAllNotes() {
        return file.notes;
    }

    public void removeSessionNote(SessionNote note) {
        file.sessionNotes.remove(note);
    }

    public void addSessionNote(int position, SessionNote note) {
        file.sessionNotes.add(position, note);
    }

    public void addNote(int position, Note note) {
        if (position > file.notes.size()) {
            position = file.notes.size();
        }
        file.notes.add(position, note);

    }

    public List<SessionNote> getAllSessionNotes() {
        return file.sessionNotes;
    }

    public List<EncounterNote> getAllEncounterNotes() {
        return file.encounterNotes;
    }
}
