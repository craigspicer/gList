package com.nullparams.glist.models;

public class Notification {

    private String id;
    private String title;
    private String version;

    public Notification() {
        //empty constructor needed
    }

    public Notification(String id, String title, String version) {

        this.id = id;
        this.title = title;
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getVersion() {
        return version;
    }
}
