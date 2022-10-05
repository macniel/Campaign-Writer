package de.macniel.campaignwriter;

import static org.junit.jupiter.api.Assertions.*;

import de.macniel.campaignwriter.SDK.FileAccessLayerFactory;
import de.macniel.campaignwriter.SDK.FileAccessLayerInterface;
import org.junit.jupiter.api.Test;

public class FileAccessLayerTest {
    
    @Test
    public void doCreateSingletonInstance() {
        FileAccessLayerInterface fal = new FileAccessLayerFactory().get();

        assertNotNull(new FileAccessLayerFactory().get());
        assertEquals(new FileAccessLayerFactory().get(), fal);
        assertNotNull(fal.getFile());
        assertEquals(0, fal.getAllNotes().size());
    }

    @Test
    public void doCreateNoteAreFindable() {

        assertTrue(true);
    }


}
