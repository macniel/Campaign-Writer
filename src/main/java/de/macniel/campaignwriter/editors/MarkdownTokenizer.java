package de.macniel.campaignwriter.editors;

import javafx.scene.text.Text;

import java.util.ArrayList;

public class MarkdownTokenizer {

    class MarkdownToken {

        enum Type {
            BOLD,
            ITALIC,
            URL,
            STRIKETHROUGH
        }

        Text text;
        String param;
        Type type;

    }

    static ArrayList<Text> getTokens (String rawText) {
        return null;
    }

}
