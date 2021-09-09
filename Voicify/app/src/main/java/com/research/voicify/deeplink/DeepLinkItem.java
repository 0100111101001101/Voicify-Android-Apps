package com.research.voicify.deeplink;

import java.util.ArrayList;

public class DeepLinkItem {
    public String title;
    public ArrayList<String> commands = new ArrayList<>();

    /**
     * This is the constructor to create a list item for deep link
     * @param title the title ( command that user call)
     * @param commands  ( list of commands that will be executed by our service)
     */
    public DeepLinkItem(String title, ArrayList<String> commands) {
        this.title = title;
        this.commands = commands;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public void setCommands(ArrayList<String> commands) {
        this.commands = commands;
    }
}
