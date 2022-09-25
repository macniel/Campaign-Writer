package de.macniel.campaignwriter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

public class FileAccessLayerTest {
    
    @Test
    public void doCreateSingletonInstance() {
        FileAccessLayer fal = FileAccessLayer.getInstance();

        assertNotNull(FileAccessLayer.getInstance());
        assertEquals(FileAccessLayer.getInstance(), fal);
        assertNotNull(fal.getFile());
        assertEquals(0, fal.getAllNotes().size());
    }

    @Test
    public void doCreateNoteAreFindable() {
        FileAccessLayer fal = FileAccessLayer.getInstance();

        String expectedUUID = "123e4567-e89b-12d3-a456-426652340000";
        String expectedLabel = "OTHER_OBJECT";

        Note testObject = new Note("", NoteType.TEXT_NOTE, UUID.fromString(expectedUUID));
        Note otherObject = new Note(expectedLabel, NoteType.TEXT_NOTE);
        fal.addNote(0, testObject);
        fal.addNote(0, otherObject);
        assertEquals(2, fal.getAllNotes().size());

        assertEquals(otherObject, fal.getAllNotes().get(0));
        assertEquals(testObject, fal.getAllNotes().get(1));
        
        assertEquals(testObject, fal.findByReference(UUID.fromString(expectedUUID)).get());

        assertEquals(otherObject, fal.findByLabel(expectedLabel).get());
    }


}
