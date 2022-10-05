package de.macniel.campaignwriter;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.Registrable;
import de.macniel.campaignwriter.SDK.RegistryInterface;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.reflections.Reflections;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class CampaignWriterApplication extends Application {

    // Returns an arraylist of class names in a JarInputStream
    private ArrayList<String> getClassNamesFromJar(JarInputStream jarFile) throws Exception {
        ArrayList<String> classNames = new ArrayList<>();
        try {
            //JarInputStream jarFile = new JarInputStream(jarFileStream);
            JarEntry jar;

            //Iterate through the contents of the jar file
            while (true) {
                jar = jarFile.getNextJarEntry();
                if (jar == null) {
                    break;
                }
                //Pick file that has the extension of .class
                if ((jar.getName().endsWith(".class"))) {
                    String className = jar.getName().replaceAll("/", "\\.");
                    String myClass = className.substring(0, className.lastIndexOf('.'));
                    classNames.add(myClass);
                }
            }
        } catch (Exception e) {
            throw new Exception("Error while getting class names from jar", e);
        }
        return classNames;
    }

    // Returns an arraylist of class names in a JarInputStream
// Calls the above function by converting the jar path to a stream
    private ArrayList<String> getClassNamesFromJar(File jarPath) throws Exception {
        return getClassNamesFromJar(new JarInputStream(new FileInputStream(jarPath)));
    }

    private void registerModules(String path) {

        Registry registry = Registry.getInstance();

        Reflections reflections = new Reflections(path);
        Set<Class<? extends Registrable>> allClasses =
                reflections.getSubTypesOf(Registrable.class);

        System.out.println("Found " + (allClasses.size()-1) + " registrable classes in path '" + path + "'");

        for ( Class<? extends Registrable> c : allClasses) {
            if (Modifier.isAbstract(c.getModifiers())) {
                continue;
            }
            try {
                System.out.print("registering " + c.getSimpleName());
                Object actualObject = c.getConstructor().newInstance();

                Method registerMethod = c.getMethod("register", RegistryInterface.class);
                registerMethod.invoke(actualObject, registry);
                System.out.println(" ... success");

            } catch (InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.out.println(" ... failure");
            }
        }
    }

    void loadExternalModules() throws MalformedURLException {
            File[] plugins = new File(Paths.get(System.getProperty("user.home"), ".campaignwriter", "plugins").toUri()).listFiles(file -> file.getName().endsWith(".jar"));
            if ( plugins == null ) {
                return;
            }

            for (File plugin : plugins) {
                URL[] arr = new URL[]{plugin.toURI().toURL()};

                try (URLClassLoader loader = new URLClassLoader(arr)) {

                    ArrayList<String> classNames = getClassNamesFromJar(plugin);

                    for (String className : classNames) {
                        if (className.startsWith("module-info")) {
                            continue;
                        }
                        try {
                            Class cc = loader.loadClass(className);
                            System.out.println(cc.toString());
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }

                    Enumeration<URL> e = loader.getResources("META-INFO/Plugin.properties");
                    e.asIterator().forEachRemaining(c -> {
                        Properties pluginProperties = new Properties();

                        try (InputStream in = c.openStream()) {
                            pluginProperties.load(in);

                            String classPath = (String) pluginProperties.get("entry-point");


                            Class<Registrable> clazz = (Class<Registrable>) loader.loadClass(classPath);
                            Registrable registrable = clazz.getDeclaredConstructor().newInstance();
                            registrable.register(Registry.getInstance());

                        } catch (IOException | ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException ignored) {
                        }


                    });
                } catch (Exception ignored) {}
            }

        }

    @Override
    public void start(Stage stage) throws IOException {

        // setting up factory
        new FileAccessLayerFactory().register(FileAccessLayer.getInstance());

        loadExternalModules();

        // Built-In Modules
        registerModules("de.macniel.campaignwriter.editors");
        registerModules("de.macniel.campaignwriter.modules");
        registerModules("de.macniel.campaignwriter.providers");



        FXMLLoader fxmlLoader = new FXMLLoader(CampaignWriterApplication.class.getResource("main-view.fxml"));
        fxmlLoader.setResources(ResourceBundle.getBundle("i18n.base"));

        new FileAccessLayerFactory().get().getGlobal("width").ifPresent(loadedWidth -> {
            stage.setWidth(Double.valueOf(loadedWidth));
        });
        new FileAccessLayerFactory().get().getGlobal("height").ifPresent(loadedHeight -> {
            stage.setHeight(Double.valueOf(loadedHeight));
        });
    
        Scene scene = new Scene(fxmlLoader.load());

        
        stage.heightProperty().addListener( (observable, oldHeight, newHeight) -> {
            new FileAccessLayerFactory().get().updateGlobal("height", newHeight.toString());
        });
        stage.widthProperty().addListener( (observable, oldWidth, newWidth) -> {
            new FileAccessLayerFactory().get().updateGlobal("width", newWidth.toString());
        });
        stage.xProperty().addListener( (observable, oldX, newX) -> {
            new FileAccessLayerFactory().get().updateGlobal("x", newX.toString());
        });
        stage.yProperty().addListener( (observable, oldY, newY) -> {
            new FileAccessLayerFactory().get().updateGlobal("y", newY.toString());
        });

        new FileAccessLayerFactory().get().getGlobal("x").ifPresent(x -> {
            stage.setX(Double.valueOf(x));
        });

        new FileAccessLayerFactory().get().getGlobal("y").ifPresent(y -> {
            stage.setY(Double.valueOf(y));
        });

        MainController controller = fxmlLoader.getController();
        scene.getStylesheets().add(CampaignWriterApplication.class.getResource("note-editor.css").toExternalForm());

        controller.getTitle().addListener( (change, oldValue, newValue) -> {
            if (newValue != null && newValue.isEmpty()) {
                stage.setTitle("Campaign Writer - " + newValue);
            } else {
                stage.setTitle("Campaign Writer");
            }
        });

        controller.setStage(stage);

        new FileAccessLayerFactory().get().getGlobal("lastFilePath").ifPresent(lastFilePath -> {

                controller.openCampaign(new File(lastFilePath));
            
        });

        System.out.println(new FileAccessLayerFactory().get().getTemplates().size() + " actor templates loaded");


        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_512.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_256.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_128.png")));
        stage.getIcons().add(new Image(CampaignWriterApplication.class.getResourceAsStream("paint_the_world_32.png")));


        stage.setTitle("Campaign Writer");

        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}