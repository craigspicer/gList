package com.nullparams.glist.models;

public class List {

    private String id;
    private String title;
    private long timeStamp;
    private String fromEmailAddress;
    private String version;

    public List() {
        //empty constructor needed
    }

    public List(String id, String title, long timeStamp, String fromEmailAddress, String version) {

        this.id = id;
        this.title = title;
        this.timeStamp = timeStamp;
        this.fromEmailAddress = fromEmailAddress;
        this.version = version;
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

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public String getVersion() {
        return version;
    }
}
