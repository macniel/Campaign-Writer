package de.macniel.campaignwriter.SDK;

import java.util.*;

public interface CampaignFileInterface {

    /**
     * Contains all notes, use filter to get renderable items
     * @return
     */
     List<? extends Note> getNotes();

    /**
     * Contains all binary Images, no garbage collection is enforced
     * @return
     */
     Map<String, String> getBase64Assets();

    /**
     * CampaignFile specific settings e.g. last module opened, last note opened...
     * @return
     */
     Properties getSettings();

}
