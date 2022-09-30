package de.macniel.campaignwriter.SDK;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Note<T> {

    public String label;
    public String type;
    public UUID reference;

    public Date createdDate;

    public Date lastModifiedDate;

    public T content;

    public int level;

    public Note(String label, String type) {
        this(label, type, UUID.randomUUID());
    }

    public static void add(int dragPosition, Note dragElement) {
        dataset.add(dragPosition, dragElement);
    }

    public UUID getReference() {
        return this.reference;
    }

    public Date getCreationDate() {
        return createdDate;
    }

    static ArrayList<Note> dataset = new ArrayList<>();

    public Note(String label, String type, UUID reference, Date createdDate, Date lastModifiedDate) {
        this.label = label;
        this.type = type;
        this.reference = reference;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
        this.level = 0;
        dataset.add(this);
    }

    public Note() {
        this.reference = UUID.randomUUID();
        dataset.add(this);
    }

    public Note(String label, String type, UUID reference) {
        this(label,type, reference, new Date(), new Date());
    }

    public Note(String label, UUID reference) {
        this(label, "text", reference);
    }

    public String getLabel() {
        return this.label;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public T getContent() {
        return content;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setContent(T content) {
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

    public String getType() {
        return type;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLabel(String s) {
        this.label = s;
    }

    @Override
    public String toString() {
        if (label != null) {
            return label;
        } else {
            return "UNKNOWN";
        }
    }
}
