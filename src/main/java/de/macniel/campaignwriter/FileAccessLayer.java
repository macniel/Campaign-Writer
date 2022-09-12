package de.macniel.campaignwriter;

import java.io.*;
import java.util.ArrayList;

public class FileAccessLayer {

    public static ArrayList<Note> loadFromFile(File f) throws IOException {
        ObjectInputStream ois = null;
        ArrayList<Note> concrete = null;
        try {
            Note.removeAll();
            ois = new ObjectInputStream(new FileInputStream(f));
            concrete = (ArrayList<Note>) ois.readObject();
            concrete.forEach( note -> Note.addOrphanedNote(note));
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert ois != null;
            ois.close();
        }
        return concrete;
    }

    public static void saveToFile(File f, ArrayList<Note> notes) throws IOException {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(notes);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            assert oos != null;
            oos.flush();
            oos.close();
        }
    }

}
