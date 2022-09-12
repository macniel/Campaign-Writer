package de.macniel.campaignwriter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Note implements Serializable {

    public String label;
    public NoteType type;
    public UUID reference;

    public Date createdDate;

    public Date lastModifiedDate;

    public String content;

    public int level;

    public int position;

    public Note(String label, NoteType type) {
        this(label, type, UUID.randomUUID());
    }

    public static void remove(Note selectedNote) {
        if (findByReference(selectedNote.getReference()) != null) {
            dataset.remove(selectedNote);
        }
    }

    public static void removeAll() {
        dataset.clear();
    }

    public static ArrayList<Note> getAll() {
        dataset.sort( (Note a, Note b) -> {
            return a.getPosition() - b.getPosition();
        });
        return dataset;
    }

    public UUID getReference() {
        return this.reference;
    }

    static ArrayList<Note> dataset = new ArrayList<>();

    static Note findByReference(UUID ref) {
        return dataset.stream().filter( note -> note.getReference().equals(ref) ).findFirst().get();
    }

    public Note(String label, NoteType type, UUID reference, Date createdDate, Date lastModifiedDate, String content) {
        this.label = label;
        this.type = type;
        this.reference = reference;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.content = content;
        this.level = 0;
        dataset.add(this);
    }

    public Note() {
        System.out.println("Default constructor for rehydration");
        dataset.add(this);
    }

    public Note(String label, NoteType type, UUID reference) {
        this(label,type, reference, new Date(), new Date(), "");
    }

    public Note(String label, UUID reference) {
        this(label, NoteType.TEXT_NOTE, reference);
    }

    public String getLabel() {
        return this.label;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public String getContent() {
        return content;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setContent(String content) {
        this.content = content;
        lastModifiedDate = new Date();
    }

    public void increaseLevel() {
        this.level++;
    }

    public void decreaseLevel() {
        if (this.level > 0) {
            this.level--;
        } else {
            this.level = 0;
        }
    }

    public NoteType getType() {
        return type;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLabel(String s) {
        this.label = s;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
