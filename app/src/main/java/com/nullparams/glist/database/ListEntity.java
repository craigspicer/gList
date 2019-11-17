package com.nullparams.glist.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "lists_table")
public class ListEntity {

    @PrimaryKey(autoGenerate = true)
    private int autoGenId;

    private String id;
    private String title;
    private long timeStamp;
    private String fromEmailAddress;
    private String version;
    private String callingFragment;

    public ListEntity(String id, String title, long timeStamp, String fromEmailAddress, String version, String callingFragment) {

        this.id = id;
        this.title = title;
        this.timeStamp = timeStamp;
        this.fromEmailAddress = fromEmailAddress;
        this.version = version;
        this.callingFragment = callingFragment;
    }

    public void setAutoGenId(int autoGenId) {
        this.autoGenId = autoGenId;
    }

    public int getAutoGenId() {
        return autoGenId;
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

    public String getCallingFragment() {
        return callingFragment;
    }
}
