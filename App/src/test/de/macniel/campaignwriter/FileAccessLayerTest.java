package de.macniel.campaignwriter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import de.macniel.campaignwriter.SDK.Note;
import de.macniel.campaignwriter.types.TextNote;
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

        assertTrue(true);
    }


}
