package com.nullparams.glist.models;

public class List {

    private String id;
    private String title;
    private long timeStamp;
    private String version;
    private String creatingFragment;

    public List() {
        //empty constructor needed
    }

    public List(String id, String title, long timeStamp, String version, String creatingFragment) {

        this.id = id;
        this.title = title;
        this.timeStamp = timeStamp;
        this.version = version;
        this.creatingFragment = creatingFragment;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getVersion() {
        return version;
    }

    public String getCreatingFragment() {
        return creatingFragment;
    }
}
