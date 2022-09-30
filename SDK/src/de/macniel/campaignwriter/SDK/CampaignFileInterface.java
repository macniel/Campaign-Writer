package de.macniel.campaignwriter.SDK;

import java.util.*;

public interface CampaignFileInterface {


    public List<? extends Note> getNotes();

    public Map<String, String> getBase64Assets();

    public Properties getSettings();

}
