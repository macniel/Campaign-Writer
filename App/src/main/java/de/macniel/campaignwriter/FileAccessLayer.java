package de.macniel.campaignwriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.macniel.campaignwriter.SDK.FileAccessLayerInterface;
import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.adapters.ColorAdapter;
import de.macniel.campaignwriter.SDK.types.Actor;
import de.macniel.campaignwriter.adapters.Point2DAdapter;
import javafx.application.HostServices;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FileAccessLayer implements FileAccessLayerInterface {

    public static final String HITPOINTS_FIELD_NAME = "Hit Points";
    public static final String PORTRAIT_FIELD_NAME = "Portrait";
    public static final String NAME_FIELD_NAME = "Name";

    private static final FileAccessLayer instance = new FileAccessLayer();
    private final Gson gsonParser;
    private CampaignFile file;
    private Properties config;
    private File confFile;
    private File keepFile;
    private HostServices hostServices;

    private FileAccessLayer() {
        file = new CampaignFile();
        file.base64Assets = new HashMap<String, String>();
        file.notes = new ArrayList<>();
        gsonParser = new GsonBuilder()
                .registerTypeAdapter(Note.class, new NoteAdapter())
                .registerTypeAdapter(Color.class, new ColorAdapter())
                .registerTypeAdapter(Point2D.class, new Point2DAdapter())
                .setPrettyPrinting()
                .create();


        initConfFile();
    }

    public static FileAccessLayer getInstance() {
        return instance;
    }

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    public Gson getParser() {
        return gsonParser;
    }

    private void initConfFile() {
        try {
            confFile = new File(Paths.get(System.getProperty("user.home"), ".campaignwriter", "config").toUri());
            if (!confFile.exists()) {
                File configFolder = new File(Paths.get(System.getProperty("user.home", ".campaignwriter")).toUri());
                //configFolder.createNewFile();
                boolean created = configFolder.mkdirs();
                if (created) {
                    confFile = new File(Paths.get(System.getProperty("user.home"), ".campaignwriter", "config").toUri());
                    confFile.createNewFile();
                    this.config = new Properties();
                    this.config.load(new FileInputStream(confFile));
                } else if (configFolder.exists()) {
                    System.err.println("Folder already exists");
                    confFile = new File(Paths.get(System.getProperty("user.home"), ".campaignwriter", "config").toUri());
                    confFile.createNewFile();
                    this.config = new Properties();
                    this.config.load(new FileInputStream(confFile));
                } else {
                    System.err.println("Couldn't create folder " + configFolder.getAbsoluteFile());
                }
            } else {
                this.config = new Properties();
                this.config.load(new FileInputStream(confFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateGlobal(String key, String value) {
        if (config == null) {
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
        if (config == null) {
            initConfFile();
        }

        return Optional.ofNullable(config.getProperty(key));

    }

    public Properties getSettings() {
        if (file != null) {
            return file.settings;
        }
        return null;
    }

    public void updateSetting(String key, String newValue) {
        if (file != null) {
            file.getSettings().setProperty(key, newValue);
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

    public Optional<String> getSetting(String key) {
        return Optional.ofNullable(file.getSettings().getProperty(key));
    }

    public HashMap<String, Actor> getTemplates() {

        File templateDir = Paths.get(System.getProperty("user.home"), ".campaignwriter", "templates").toFile();
        if (templateDir.exists() && templateDir.isDirectory()) {
            HashMap<String, Actor> templates = new HashMap<>();

            for (File f : templateDir.listFiles()) {

                Actor fromFile;
                try {
                    fromFile = gsonParser.fromJson(new JsonReader(new FileReader(f)), Actor.class);
                    templates.put(f.getName(), fromFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return templates;
        } else {
            templateDir.mkdirs();
            return new HashMap<>();
        }
    }

    public Optional<Map.Entry<String, Image>> getImageFromString(String s) { // FIXME: Images arent saved
        if (s == null) {
            return Optional.empty();
        }

        if (new File(s).exists()) {
            try {
                String uuid = UUID.randomUUID().toString();
                byte[] input = Files.readAllBytes(Paths.get(s));
                String base64Asset = new String(Base64.getEncoder().encode(input));
                InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Asset.getBytes(StandardCharsets.UTF_8)));
                Image image = new Image(in);
                System.out.println("Storing Image from path " + s + " as UUID " + uuid);
                file.base64Assets.put(uuid, base64Asset);
                return Optional.ofNullable(new AbstractMap.SimpleEntry<>(uuid, image));
            } catch (Exception e) {
            }
        }

        try {
            String base64Asset = file.base64Assets.get(s);
            InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Asset.getBytes(StandardCharsets.UTF_8)));
            Image image = new Image(in);
            return Optional.ofNullable(new AbstractMap.SimpleEntry<>(s, image));
        } catch (Exception e) {
        }

        try {
            URL url = new URL(s);

            try (InputStream reader = url.openStream()) {
                String uuid = UUID.randomUUID().toString();
                byte[] input = reader.readAllBytes();
                String base64Asset = new String(Base64.getEncoder().encode(input));
                InputStream in = Base64.getDecoder().wrap(new ByteArrayInputStream(base64Asset.getBytes(StandardCharsets.UTF_8)));
                Image image = new Image(in);
                System.out.println("Storing Image from path " + s + " as UUID " + uuid);
                file.base64Assets.put(uuid, base64Asset);
                return Optional.ofNullable(new AbstractMap.SimpleEntry<>(uuid, image));
            } catch (Exception ignored) {
            }

        } catch (MalformedURLException ignored) {
        }


        return Optional.empty();

    }

    public void saveToFile(File f) throws IOException {
        keepFile = f;
        try (FileWriter writer = new FileWriter((f))) {
            System.out.println("IO write json to file " + f.getAbsoluteFile());
            System.out.println("  writing " + file.getNotes() + " notes");

            gsonParser.toJson(file, writer);
            updateGlobal("lastFilePath", f.getAbsolutePath());
            writer.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public void saveToFile() throws IOException {
        System.out.println("fal requested to save");
        if (keepFile != null) {
            saveToFile(keepFile);
        }
    }

    public void loadFromFile(File f) throws IOException {
        keepFile = f;
        file = gsonParser.fromJson(new JsonReader(new FileReader(f)), CampaignFile.class);
        System.out.println("loaded " + file.getNotes().size() + " notes from file");
        updateGlobal("lastFilePath", f.getAbsolutePath());
    }

    public CampaignFile getFile() {
        return file;
    }

    public void newCampaign() {
        file = new CampaignFile();
    }

    public Optional<Note> findByLabel(String label) {
        System.out.println("searching for Note with Label " + label);
        return file.notes.stream().filter(note -> note.getLabel().equals(label)).findFirst();
    }

    public Optional<Note> findByReference(UUID ref) {
        System.out.println("searching for " + ref);
        return file.notes.stream().filter(Objects::nonNull).filter(note -> note.getReference().equals(ref)).findFirst();
    }

    public void removeNote(Note selectedNote) {
        findByReference(selectedNote.getReference()).ifPresent(note -> {
            file.notes.remove(selectedNote);
        });
    }

    public void removeAll() {
        file.notes.clear();
    }

    public List<Note> getAllNotes() {
        return file.notes;
    }

    public void addNote(int position, Note note) {
        if (position > file.notes.size()) {
            position = file.notes.size();
        }
        file.notes.add(position, note);

    }

    public String getPluginFolderPath() {
        return Paths.get(System.getProperty("user.home"), ".campaignwriter", "templates").toFile().getAbsolutePath();
    }
}
