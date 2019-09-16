package io.mrarm.irc.newui.group;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableList;

public class CustomGroup extends BaseGroup {

    private String name;
    private final ObservableList<ServerChannelPair> channels = new ObservableArrayList<>();

    public CustomGroup(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public ObservableList<ServerChannelPair> getChannels() {
        return channels;
    }

}